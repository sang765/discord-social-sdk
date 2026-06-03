package com.discord.sdk.core

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.*
import java.io.EOFException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

internal class WebSocketClient(
    private val httpClient: DiscordHttpClient,
    private val url: String,
    private val scope: CoroutineScope
) {
    private var webSocket: WebSocket? = null
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5
    private var shouldReconnect = true

    private val _events = Channel<String>(Channel.BUFFERED)
    val events: Flow<String> = flow {
        for (event in _events) {
            emit(event)
        }
    }

    fun connect() {
        shouldReconnect = true
        webSocket = httpClient.createWebSocket(url, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                reconnectAttempts = 0
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                scope.launch {
                    _events.send(text)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(code, reason)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (shouldReconnect) {
                    scheduleReconnect()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                if (t is EOFException || t.message?.contains("reset") == true) {
                    if (shouldReconnect) {
                        scheduleReconnect()
                    }
                }
            }
        })
    }

    fun send(message: String): Boolean {
        return webSocket?.send(message) ?: false
    }

    fun disconnect() {
        shouldReconnect = false
        reconnectAttempts = maxReconnectAttempts
        webSocket?.close(1000, "Client closing")
        webSocket = null
    }

    private fun scheduleReconnect() {
        if (reconnectAttempts >= maxReconnectAttempts) return
        reconnectAttempts++
        val delay = (1 shl reconnectAttempts) * 1000L
        scope.launch {
            delay(delay)
            connect()
        }
    }
}
