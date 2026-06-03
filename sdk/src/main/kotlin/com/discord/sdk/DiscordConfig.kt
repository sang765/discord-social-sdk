package com.discord.sdk

import com.discord.sdk.gateway.GatewayIntent

data class DiscordConfig(
    val clientId: String,
    val clientSecret: String? = null,
    val token: String? = null,
    val intents: Set<GatewayIntent> = setOf(
        GatewayIntent.GUILDS,
        GatewayIntent.GUILD_MESSAGES,
        GatewayIntent.DIRECT_MESSAGES
    ),
    val apiBaseUrl: String = "https://discord.com/api/v10",
    val gatewayUrl: String = "wss://gateway.discord.gg/?v=10&encoding=json",
    val oauthUrl: String = "https://discord.com/api/oauth2",
    val userAgent: String = "DiscordSocialSDK/1.0 (Android)",
    val connectTimeoutMs: Long = 10_000,
    val readTimeoutMs: Long = 10_000,
    val writeTimeoutMs: Long = 10_000,
    val maxRetries: Int = 3
)
