package com.discord.sdk.api

import com.discord.sdk.core.DiscordHttpClient
import com.discord.sdk.model.*

class RelationshipApi internal constructor(
    private val http: DiscordHttpClient
) {
    suspend fun getRelationships(): Result<List<Relationship>> = runCatching {
        val json = http.get("/users/@me/relationships").getOrThrow()
        http.json.decodeFromString<List<Relationship>>(json)
    }

    suspend fun getRelationship(userId: Snowflake): Result<Relationship> = runCatching {
        val json = http.get("/users/@me/relationships/${userId.value}").getOrThrow()
        http.json.decodeFromString<Relationship>(json)
    }

    suspend fun sendFriendRequest(username: String, discriminator: String): Result<Unit> = runCatching {
        val body = http.json.encodeToString(mapOf("username" to username, "discriminator" to discriminator))
        http.post("/users/@me/relationships", body).getOrThrow()
    }

    suspend fun sendFriendRequestById(userId: Snowflake): Result<Unit> = runCatching {
        val body = http.json.encodeToString(mapOf("recipient_id" to userId.value))
        http.post("/users/@me/relationships", body).getOrThrow()
    }

    suspend fun acceptFriendRequest(userId: Snowflake): Result<Unit> = runCatching {
        val body = http.json.encodeToString(mapOf("type" to "1"))
        http.put("/users/@me/relationships/${userId.value}", body).getOrThrow()
    }

    suspend fun removeRelationship(userId: Snowflake): Result<Unit> = runCatching {
        http.delete("/users/@me/relationships/${userId.value}").getOrThrow()
    }

    suspend fun blockUser(userId: Snowflake): Result<Unit> = runCatching {
        val body = http.json.encodeToString(mapOf("type" to "2"))
        http.put("/users/@me/relationships/${userId.value}", body).getOrThrow()
    }

    suspend fun unblockUser(userId: Snowflake): Result<Unit> = runCatching {
        http.delete("/users/@me/relationships/${userId.value}").getOrThrow()
    }
}
