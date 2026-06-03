package com.discord.sdk

import com.discord.sdk.api.*
import com.discord.sdk.auth.DiscordOAuth2
import com.discord.sdk.core.DiscordHttpClient
import com.discord.sdk.gateway.GatewayClient
import com.discord.sdk.gateway.GatewayEvent
import com.discord.sdk.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow

class DiscordClient(
    config: DiscordConfig,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) : AutoCloseable {
    private val httpClient = DiscordHttpClient(config)

    val userApi: UserApi = UserApi(httpClient)
    val guildApi: GuildApi = GuildApi(httpClient)
    val channelApi: ChannelApi = ChannelApi(httpClient)
    val voiceApi: VoiceApi = VoiceApi(httpClient)
    val relationshipApi: RelationshipApi = RelationshipApi(httpClient)
    val oauth2: DiscordOAuth2 = DiscordOAuth2(config)

    private val gatewayClient = GatewayClient(config, httpClient)

    val gatewayEvents: Flow<GatewayEvent>
        get() = gatewayClient.events

    fun connectGateway() {
        gatewayClient.connect()
    }

    fun disconnectGateway() {
        gatewayClient.disconnect()
    }

    fun updatePresence(status: String = "online", activity: Activity? = null) {
        gatewayClient.sendPresence(status, activity)
    }

    fun isGatewayConnected(): Boolean = gatewayClient.isConnected()

    override fun close() {
        gatewayClient.destroy()
        httpClient.shutdown()
    }

    class Builder {
        private var clientId: String = ""
        private var clientSecret: String? = null
        private var token: String? = null
        private var intents: Set<com.discord.sdk.gateway.GatewayIntent> = setOf(
            com.discord.sdk.gateway.GatewayIntent.GUILDS,
            com.discord.sdk.gateway.GatewayIntent.GUILD_MESSAGES,
            com.discord.sdk.gateway.GatewayIntent.DIRECT_MESSAGES
        )
        private var apiBaseUrl: String = "https://discord.com/api/v10"
        private var gatewayUrl: String = "wss://gateway.discord.gg/?v=10&encoding=json"

        fun setClientId(clientId: String) = apply { this.clientId = clientId }
        fun setClientSecret(secret: String) = apply { this.clientSecret = secret }
        fun setToken(token: String) = apply { this.token = token }
        fun setIntents(intents: Set<com.discord.sdk.gateway.GatewayIntent>) = apply { this.intents = intents }
        fun setApiBaseUrl(url: String) = apply { this.apiBaseUrl = url }
        fun setGatewayUrl(url: String) = apply { this.gatewayUrl = url }

        fun build(): DiscordClient {
            val config = DiscordConfig(
                clientId = clientId,
                clientSecret = clientSecret,
                token = token,
                intents = intents,
                apiBaseUrl = apiBaseUrl,
                gatewayUrl = gatewayUrl
            )
            return DiscordClient(config)
        }
    }
}
