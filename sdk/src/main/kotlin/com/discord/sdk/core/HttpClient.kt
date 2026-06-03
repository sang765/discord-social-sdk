package com.discord.sdk.core

import com.discord.sdk.DiscordConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class DiscordHttpClient(
    private val config: DiscordConfig
) {
    private val jsonMediaType = "application/json".toMediaType()

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        coerceInputValues = true
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (java.lang.Boolean.getBoolean("discord.sdk.debug"))
            HttpLoggingInterceptor.Level.BODY
        else
            HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(config.connectTimeoutMs, TimeUnit.MILLISECONDS)
        .readTimeout(config.readTimeoutMs, TimeUnit.MILLISECONDS)
        .writeTimeout(config.writeTimeoutMs, TimeUnit.MILLISECONDS)
        .addInterceptor(loggingInterceptor)
        .addInterceptor(RateLimitInterceptor())
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", config.userAgent)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
            config.token?.let {
                val authScheme = if (config.isBot) "Bot" else "Bearer"
                request.header("Authorization", "$authScheme $it")
            }
            chain.proceed(request.build())
        }
        .build()

    suspend fun get(path: String): Result<String> = execute("GET", path, null)

    suspend fun post(path: String, body: String? = null): Result<String> =
        execute("POST", path, body)

    suspend fun put(path: String, body: String? = null): Result<String> =
        execute("PUT", path, body)

    suspend fun patch(path: String, body: String? = null): Result<String> =
        execute("PATCH", path, body)

    suspend fun delete(path: String): Result<String> = execute("DELETE", path, null)

    private suspend fun execute(
        method: String,
        path: String,
        body: String?
    ): Result<String> = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            val url = config.apiBaseUrl + path
            val bodyObj = body?.toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .method(method, bodyObj)
                .build()

            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(e)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (continuation.isActive) {
                        val bodyString = response.body?.string() ?: ""
                        if (response.isSuccessful) {
                            continuation.resume(Result.success(bodyString))
                        } else {
                            continuation.resume(
                                Result.failure(
                                    DiscordApiException(
                                        statusCode = response.code,
                                        body = bodyString,
                                        message = "HTTP ${response.code}: ${response.message}"
                                    )
                                )
                            )
                        }
                        response.close()
                    }
                }
            })
        }
    }

    fun createWebSocket(
        url: String,
        listener: WebSocketListener
    ): WebSocket {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", config.userAgent)
            .build()
        return okHttpClient.newWebSocket(request, listener)
    }

    fun shutdown() {
        okHttpClient.dispatcher.executorService.shutdown()
        okHttpClient.connectionPool.evictAll()
    }
}

class DiscordApiException(
    val statusCode: Int,
    val body: String,
    override val message: String
) : Exception(message)

private class RateLimitInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 429) {
            val retryAfter = response.header("Retry-After")?.toLongOrNull() ?: 1L
            Thread.sleep(retryAfter * 1000)
            response.close()
            return chain.proceed(chain.request())
        }
        return response
    }
}
