package com.discord.sdk.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.discord.sdk.DiscordConfig
import com.discord.sdk.model.Snowflake
import com.discord.sdk.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DiscordOAuth2(
    private val config: DiscordConfig
) {
    private val client = OkHttpClient()
    private val mediaType = "application/x-www-form-urlencoded".toMediaType()

    companion object {
        private const val AUTHORIZE_URL = "https://discord.com/api/oauth2/authorize"
        private const val TOKEN_URL = "https://discord.com/api/oauth2/token"
        private const val REVOKE_URL = "https://discord.com/api/oauth2/token/revoke"

        val DEFAULT_SCOPES = listOf(
            "identify", "email", "guilds",
            "messages.read", "gdm.join",
            "rpc.activities.write", "relationships.read"
        )
    }

    data class OAuth2Result(
        val accessToken: String,
        val refreshToken: String?,
        val expiresIn: Long,
        val scope: String,
        val tokenType: String = "Bearer"
    )

    fun getAuthorizationUrl(
        redirectUri: String,
        scopes: List<String> = DEFAULT_SCOPES,
        state: String = generateState()
    ): String {
        val uri = Uri.parse(AUTHORIZE_URL).buildUpon()
            .appendQueryParameter("client_id", config.clientId)
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("scope", scopes.joinToString(" "))
            .appendQueryParameter("state", state)
        return uri.build().toString()
    }

    fun getAuthorizationIntent(
        redirectUri: String,
        scopes: List<String> = DEFAULT_SCOPES,
        state: String = generateState()
    ): Intent {
        return Intent(Intent.ACTION_VIEW)
            .setData(Uri.parse(getAuthorizationUrl(redirectUri, scopes, state)))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    suspend fun exchangeCode(
        code: String,
        redirectUri: String
    ): Result<OAuth2Result> = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            val body = FormBody.Builder()
                .add("client_id", config.clientId)
                .add("client_secret", config.clientSecret ?: "")
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", redirectUri)
                .build()

            val request = Request.Builder()
                .url(TOKEN_URL)
                .post(body)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(e)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (continuation.isActive) {
                        try {
                            val bodyStr = response.body?.string() ?: ""
                            if (!response.isSuccessful) {
                                continuation.resume(
                                    Result.failure(Exception("OAuth2 error: $bodyStr"))
                                )
                                return
                            }
                            val json = com.discord.sdk.core.DiscordHttpClient(config).json
                            val tokenResp = json.decodeFromString<TokenResponse>(bodyStr)
                            continuation.resume(
                                Result.success(
                                    OAuth2Result(
                                        accessToken = tokenResp.accessToken,
                                        refreshToken = tokenResp.refreshToken,
                                        expiresIn = tokenResp.expiresIn,
                                        scope = tokenResp.scope,
                                        tokenType = tokenResp.tokenType
                                    )
                                )
                            )
                        } catch (e: Exception) {
                            continuation.resume(Result.failure(e))
                        } finally {
                            response.close()
                        }
                    }
                }
            })
        }
    }

    suspend fun refreshToken(refreshToken: String): Result<OAuth2Result> = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            val body = FormBody.Builder()
                .add("client_id", config.clientId)
                .add("client_secret", config.clientSecret ?: "")
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .build()

            val request = Request.Builder()
                .url(TOKEN_URL)
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isActive) continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (continuation.isActive) {
                        try {
                            val bodyStr = response.body?.string() ?: ""
                            if (!response.isSuccessful) {
                                continuation.resume(Result.failure(Exception("Token refresh error: $bodyStr")))
                                return
                            }
                            val json = com.discord.sdk.core.DiscordHttpClient(config).json
                            val tokenResp = json.decodeFromString<TokenResponse>(bodyStr)
                            continuation.resume(
                                Result.success(
                                    OAuth2Result(
                                        accessToken = tokenResp.accessToken,
                                        refreshToken = tokenResp.refreshToken,
                                        expiresIn = tokenResp.expiresIn,
                                        scope = tokenResp.scope
                                    )
                                )
                            )
                        } catch (e: Exception) {
                            continuation.resume(Result.failure(e))
                        } finally {
                            response.close()
                        }
                    }
                }
            })
        }
    }

    suspend fun revokeToken(accessToken: String): Result<Unit> = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            val body = FormBody.Builder()
                .add("client_id", config.clientId)
                .add("client_secret", config.clientSecret ?: "")
                .add("token", accessToken)
                .build()

            val request = Request.Builder()
                .url(REVOKE_URL)
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isActive) continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.close()
                    if (continuation.isActive) {
                        continuation.resume(Result.success(Unit))
                    }
                }
            })
        }
    }

    suspend fun getCurrentUser(accessToken: String): Result<User> = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            val request = Request.Builder()
                .url("https://discord.com/api/v10/users/@me")
                .header("Authorization", "Bearer $accessToken")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isActive) continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (continuation.isActive) {
                        try {
                            val body = response.body?.string() ?: ""
                            if (!response.isSuccessful) {
                                continuation.resume(Result.failure(Exception("API error: $body")))
                                return
                            }
                            val json = com.discord.sdk.core.DiscordHttpClient(config).json
                            val user = json.decodeFromString<User>(body)
                            continuation.resume(Result.success(user))
                        } catch (e: Exception) {
                            continuation.resume(Result.failure(e))
                        } finally {
                            response.close()
                        }
                    }
                }
            })
        }
    }

    fun parseRedirectUri(uri: Uri): CodeResult? {
        val code = uri.getQueryParameter("code") ?: return null
        val state = uri.getQueryParameter("state")
        return CodeResult(code = code, state = state)
    }

    data class CodeResult(val code: String, val state: String?)

    companion object {
        fun generateState(): String {
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            return (1..32).map { chars.random() }.joinToString("")
        }
    }
}

@Serializable
data class TokenResponse(
    @kotlinx.serialization.SerialName("access_token") val accessToken: String,
    @kotlinx.serialization.SerialName("token_type") val tokenType: String,
    @kotlinx.serialization.SerialName("expires_in") val expiresIn: Long,
    @kotlinx.serialization.SerialName("refresh_token") val refreshToken: String? = null,
    val scope: String
)
