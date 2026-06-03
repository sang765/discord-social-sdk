package com.discord.sdk.api

import com.discord.sdk.core.DiscordHttpClient
import com.discord.sdk.model.*

class VoiceApi internal constructor(
    private val http: DiscordHttpClient
) {
    suspend fun getVoiceRegions(): Result<List<VoiceRegion>> = runCatching {
        val json = http.get("/voice/regions").getOrThrow()
        http.json.decodeFromString<List<VoiceRegion>>(json)
    }

    suspend fun getChannelVoiceStates(channelId: Snowflake): Result<List<VoiceState>> = runCatching {
        val json = http.get("/channels/${channelId.value}/voice-states").getOrThrow()
        http.json.decodeFromString<List<VoiceState>>(json)
    }

    suspend fun getCurrentUserVoiceState(guildId: Snowflake): Result<VoiceState> = runCatching {
        val json = http.get("/guilds/${guildId.value}/voice-states/@me").getOrThrow()
        http.json.decodeFromString<VoiceState>(json)
    }

    suspend fun getUserVoiceState(guildId: Snowflake, userId: Snowflake): Result<VoiceState> = runCatching {
        val json = http.get("/guilds/${guildId.value}/voice-states/${userId.value}").getOrThrow()
        http.json.decodeFromString<VoiceState>(json)
    }

    suspend fun modifyCurrentUserVoiceState(
        guildId: Snowflake,
        channelId: Snowflake? = null,
        suppress: Boolean? = null,
        requestToSpeakTimestamp: String? = null
    ): Result<Unit> = runCatching {
        val req = buildMap<String, Any> {
            channelId?.let { put("channel_id", it.value) }
            suppress?.let { put("suppress", it) }
            requestToSpeakTimestamp?.let { put("request_to_speak_timestamp", it) }
        }
        val body = http.json.encodeToString(req)
        http.patch("/guilds/${guildId.value}/voice-states/@me", body).getOrThrow()
    }
}
