package com.discord.sdk.api

import com.discord.sdk.core.DiscordHttpClient
import com.discord.sdk.model.*
import kotlinx.serialization.json.*

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
        val body = buildJsonObject {
            put("username", JsonPrimitive(username))
            put("discriminator", JsonPrimitive(discriminator))
        }.toString()
        http.post("/users/@me/relationships", body).getOrThrow()
    }

    suspend fun sendFriendRequestById(userId: Snowflake): Result<Unit> = runCatching {
        val body = buildJsonObject {
            put("recipient_id", JsonPrimitive(userId.value))
        }.toString()
        http.post("/users/@me/relationships", body).getOrThrow()
    }

    suspend fun acceptFriendRequest(userId: Snowflake): Result<Unit> = runCatching {
        val body = buildJsonObject {
            put("type", JsonPrimitive("1"))
        }.toString()
        http.put("/users/@me/relationships/${userId.value}", body).getOrThrow()
    }

    suspend fun removeRelationship(userId: Snowflake): Result<Unit> = runCatching {
        http.delete("/users/@me/relationships/${userId.value}").getOrThrow()
    }

    suspend fun blockUser(userId: Snowflake): Result<Unit> = runCatching {
        val body = buildJsonObject {
            put("type", JsonPrimitive("2"))
        }.toString()
        http.put("/users/@me/relationships/${userId.value}", body).getOrThrow()
    }

    suspend fun unblockUser(userId: Snowflake): Result<Unit> = runCatching {
        http.delete("/users/@me/relationships/${userId.value}").getOrThrow()
    }
}
