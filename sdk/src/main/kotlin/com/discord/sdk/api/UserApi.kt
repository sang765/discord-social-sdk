package com.discord.sdk.api

import com.discord.sdk.core.DiscordHttpClient
import com.discord.sdk.model.*

class UserApi internal constructor(
    private val http: DiscordHttpClient
) {
    suspend fun getCurrentUser(): Result<User> = runCatching {
        val json = http.get("/users/@me").getOrThrow()
        http.json.decodeFromString<User>(json)
    }

    suspend fun getUser(userId: Snowflake): Result<User> = runCatching {
        val json = http.get("/users/${userId.value}").getOrThrow()
        http.json.decodeFromString<User>(json)
    }

    suspend fun modifyCurrentUser(
        username: String? = null,
        avatar: String? = null
    ): Result<User> = runCatching {
        val body = buildMap<String, String> {
            username?.let { put("username", it) }
            avatar?.let { put("avatar", it) }
        }
        val json = http.patch("/users/@me", http.json.encodeToString(body)).getOrThrow()
        http.json.decodeFromString<User>(json)
    }

    suspend fun getCurrentUserGuilds(
        limit: Int = 200,
        before: Snowflake? = null,
        after: Snowflake? = null
    ): Result<List<Guild>> = runCatching {
        val params = mutableListOf("limit=$limit")
        before?.let { params.add("before=${it.value}") }
        after?.let { params.add("after=${it.value}") }
        val json = http.get("/users/@me/guilds?${params.joinToString("&")}").getOrThrow()
        http.json.decodeFromString<List<Guild>>(json)
    }

    suspend fun leaveGuild(guildId: Snowflake): Result<Unit> = runCatching {
        http.delete("/users/@me/guilds/${guildId.value}").getOrThrow()
    }

    suspend fun createDM(recipientId: Snowflake): Result<Channel> = runCatching {
        val body = http.json.encodeToString(mapOf("recipient_id" to recipientId.value))
        val json = http.post("/users/@me/channels", body).getOrThrow()
        http.json.decodeFromString<Channel>(json)
    }

    suspend fun createGroupDM(accessTokens: List<String>, nicks: Map<Snowflake, String>): Result<Channel> = runCatching {
        val req = mapOf(
            "access_tokens" to accessTokens,
            "nicks" to nicks.mapKeys { it.key.value }
        )
        val body = http.json.encodeToString(req)
        val json = http.post("/users/@me/channels", body).getOrThrow()
        http.json.decodeFromString<Channel>(json)
    }

    suspend fun getUserConnections(): Result<List<ConnectionAccount>> = runCatching {
        val json = http.get("/users/@me/connections").getOrThrow()
        http.json.decodeFromString<List<ConnectionAccount>>(json)
    }

    suspend fun getCurrentUserProfile(): Result<UserProfile> = runCatching {
        val json = http.get("/users/@me/profile").getOrThrow()
        http.json.decodeFromString<UserProfile>(json)
    }
}
