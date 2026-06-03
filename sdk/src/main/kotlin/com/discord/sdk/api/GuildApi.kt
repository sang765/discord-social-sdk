package com.discord.sdk.api

import com.discord.sdk.core.DiscordHttpClient
import com.discord.sdk.model.*

class GuildApi internal constructor(
    private val http: DiscordHttpClient
) {
    suspend fun getGuild(guildId: Snowflake, withCounts: Boolean = false): Result<Guild> = runCatching {
        val json = http.get("/guilds/${guildId.value}?with_counts=$withCounts").getOrThrow()
        http.json.decodeFromString<Guild>(json)
    }

    suspend fun getGuildChannels(guildId: Snowflake): Result<List<Channel>> = runCatching {
        val json = http.get("/guilds/${guildId.value}/channels").getOrThrow()
        http.json.decodeFromString<List<Channel>>(json)
    }

    suspend fun getGuildMember(guildId: Snowflake, userId: Snowflake): Result<GuildMember> = runCatching {
        val json = http.get("/guilds/${guildId.value}/members/${userId.value}").getOrThrow()
        http.json.decodeFromString<GuildMember>(json)
    }

    suspend fun listGuildMembers(
        guildId: Snowflake,
        limit: Int = 100,
        after: Snowflake? = null
    ): Result<List<GuildMember>> = runCatching {
        val params = mutableListOf("limit=$limit")
        after?.let { params.add("after=${it.value}") }
        val json = http.get("/guilds/${guildId.value}/members?${params.joinToString("&")}").getOrThrow()
        http.json.decodeFromString<List<GuildMember>>(json)
    }

    suspend fun getGuildRoles(guildId: Snowflake): Result<List<Role>> = runCatching {
        val json = http.get("/guilds/${guildId.value}/roles").getOrThrow()
        http.json.decodeFromString<List<Role>>(json)
    }

    suspend fun getGuildEmojis(guildId: Snowflake): Result<List<Emoji>> = runCatching {
        val json = http.get("/guilds/${guildId.value}/emojis").getOrThrow()
        http.json.decodeFromString<List<Emoji>>(json)
    }

    suspend fun getGuildVoiceRegions(guildId: Snowflake): Result<List<VoiceRegion>> = runCatching {
        val json = http.get("/guilds/${guildId.value}/regions").getOrThrow()
        http.json.decodeFromString<List<VoiceRegion>>(json)
    }

    suspend fun getGuildPreview(guildId: Snowflake): Result<Guild> = runCatching {
        val json = http.get("/guilds/${guildId.value}/preview").getOrThrow()
        http.json.decodeFromString<Guild>(json)
    }

    suspend fun getGuildVanityUrl(guildId: Snowflake): Result<Map<String, String>> = runCatching {
        val json = http.get("/guilds/${guildId.value}/vanity-url").getOrThrow()
        http.json.decodeFromString<Map<String, String>>(json)
    }

    suspend fun searchGuildMembers(
        guildId: Snowflake,
        query: String,
        limit: Int = 25
    ): Result<List<GuildMember>> = runCatching {
        val json = http.get("/guilds/${guildId.value}/members/search?query=$query&limit=$limit").getOrThrow()
        http.json.decodeFromString<List<GuildMember>>(json)
    }
}
