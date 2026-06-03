package com.discord.sdk.api

import com.discord.sdk.core.DiscordHttpClient
import com.discord.sdk.model.*
import kotlinx.serialization.json.*

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
        val body = buildJsonObject {
            username?.let { put("username", JsonPrimitive(it)) }
            avatar?.let { put("avatar", JsonPrimitive(it)) }
        }.toString()
        val json = http.patch("/users/@me", body).getOrThrow()
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
        val body = buildJsonObject {
            put("recipient_id", JsonPrimitive(recipientId.value))
        }.toString()
        val json = http.post("/users/@me/channels", body).getOrThrow()
        http.json.decodeFromString<Channel>(json)
    }

    suspend fun createGroupDM(accessTokens: List<String>, nicks: Map<Snowflake, String>): Result<Channel> = runCatching {
        val body = buildJsonObject {
            put("access_tokens", JsonArray(accessTokens.map { JsonPrimitive(it) }))
            put("nicks", buildJsonObject {
                nicks.forEach { (key, value) ->
                    put(key.value, JsonPrimitive(value))
                }
            })
        }.toString()
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
