package com.discord.sdk.api

import com.discord.sdk.core.DiscordHttpClient
import com.discord.sdk.model.*
import kotlinx.serialization.json.*

class ChannelApi internal constructor(
    private val http: DiscordHttpClient
) {
    suspend fun getChannel(channelId: Snowflake): Result<Channel> = runCatching {
        val json = http.get("/channels/${channelId.value}").getOrThrow()
        http.json.decodeFromString<Channel>(json)
    }

    suspend fun modifyChannel(
        channelId: Snowflake,
        name: String? = null,
        topic: String? = null,
        position: Int? = null,
        nsfw: Boolean? = null,
        rateLimitPerUser: Int? = null,
        bitrate: Int? = null,
        userLimit: Int? = null,
        parentId: Snowflake? = null
    ): Result<Channel> = runCatching {
        val req = buildJsonObject {
            name?.let { put("name", JsonPrimitive(it)) }
            topic?.let { put("topic", JsonPrimitive(it)) }
            position?.let { put("position", JsonPrimitive(it)) }
            nsfw?.let { put("nsfw", JsonPrimitive(it)) }
            rateLimitPerUser?.let { put("rate_limit_per_user", JsonPrimitive(it)) }
            bitrate?.let { put("bitrate", JsonPrimitive(it)) }
            userLimit?.let { put("user_limit", JsonPrimitive(it)) }
            parentId?.let { put("parent_id", JsonPrimitive(it.value)) }
        }
        val body = req.toString()
        val json = http.patch("/channels/${channelId.value}", body).getOrThrow()
        http.json.decodeFromString<Channel>(json)
    }

    suspend fun deleteChannel(channelId: Snowflake): Result<Unit> = runCatching {
        http.delete("/channels/${channelId.value}").getOrThrow()
    }

    suspend fun getChannelMessages(
        channelId: Snowflake,
        limit: Int = 50,
        around: Snowflake? = null,
        before: Snowflake? = null,
        after: Snowflake? = null
    ): Result<List<Message>> = runCatching {
        val params = mutableListOf("limit=$limit")
        around?.let { params.add("around=${it.value}") }
        before?.let { params.add("before=${it.value}") }
        after?.let { params.add("after=${it.value}") }
        val json = http.get("/channels/${channelId.value}/messages?${params.joinToString("&")}").getOrThrow()
        http.json.decodeFromString<List<Message>>(json)
    }

    suspend fun getChannelMessage(channelId: Snowflake, messageId: Snowflake): Result<Message> = runCatching {
        val json = http.get("/channels/${channelId.value}/messages/${messageId.value}").getOrThrow()
        http.json.decodeFromString<Message>(json)
    }

    suspend fun createMessage(
        channelId: Snowflake,
        request: MessageCreateRequest
    ): Result<Message> = runCatching {
        val req = buildJsonObject {
            request.content?.let { put("content", JsonPrimitive(it)) }
            request.tts?.let { put("tts", JsonPrimitive(it)) }
            request.components?.let { put("components", JsonArray(it.map { componentToJson(it) })) }
            request.stickerIds?.let { put("sticker_ids", JsonArray(it.map { JsonPrimitive(it.value) })) }
            request.flags?.let { put("flags", JsonPrimitive(it)) }
            request.embeds?.let { put("embeds", JsonArray(it.map { embedToJson(it) })) }
            request.allowedMentions?.let {
                put("allowed_mentions", buildJsonObject {
                    put("parse", JsonArray(it.parse.map { p -> JsonPrimitive(p) }))
                    put("roles", JsonArray(it.roles.map { r -> JsonPrimitive(r.value) }))
                    put("users", JsonArray(it.users.map { u -> JsonPrimitive(u.value) }))
                    put("replied_user", JsonPrimitive(it.repliedUser))
                })
            }
            request.messageReference?.let {
                put("message_reference", buildJsonObject {
                    put("message_id", JsonPrimitive(it.messageId.value))
                    it.channelId?.let { c -> put("channel_id", JsonPrimitive(c.value)) }
                    it.guildId?.let { g -> put("guild_id", JsonPrimitive(g.value)) }
                    put("fail_if_not_exists", JsonPrimitive(it.failIfNotExists))
                })
            }
        }
        val body = req.toString()
        val json = http.post("/channels/${channelId.value}/messages", body).getOrThrow()
        http.json.decodeFromString<Message>(json)
    }

    suspend fun deleteMessage(channelId: Snowflake, messageId: Snowflake): Result<Unit> = runCatching {
        http.delete("/channels/${channelId.value}/messages/${messageId.value}").getOrThrow()
        Unit
    }

    suspend fun bulkDeleteMessages(channelId: Snowflake, messageIds: List<Snowflake>): Result<Unit> = runCatching {
        val body = buildJsonObject {
            put("messages", JsonArray(messageIds.map { JsonPrimitive(it.value) }))
        }.toString()
        http.post("/channels/${channelId.value}/messages/bulk-delete", body).getOrThrow()
    }

    suspend fun triggerTypingIndicator(channelId: Snowflake): Result<Unit> = runCatching {
        http.post("/channels/${channelId.value}/typing").getOrThrow()
    }

    suspend fun getPinnedMessages(channelId: Snowflake): Result<List<Message>> = runCatching {
        val json = http.get("/channels/${channelId.value}/pins").getOrThrow()
        http.json.decodeFromString<List<Message>>(json)
    }

    suspend fun pinMessage(channelId: Snowflake, messageId: Snowflake): Result<Unit> = runCatching {
        http.put("/channels/${channelId.value}/pins/${messageId.value}").getOrThrow()
    }

    suspend fun unpinMessage(channelId: Snowflake, messageId: Snowflake): Result<Unit> = runCatching {
        http.delete("/channels/${channelId.value}/pins/${messageId.value}").getOrThrow()
    }

    suspend fun addReaction(channelId: Snowflake, messageId: Snowflake, emoji: String): Result<Unit> = runCatching {
        http.put("/channels/${channelId.value}/messages/${messageId.value}/reactions/$emoji/@me").getOrThrow()
    }

    suspend fun removeOwnReaction(channelId: Snowflake, messageId: Snowflake, emoji: String): Result<Unit> = runCatching {
        http.delete("/channels/${channelId.value}/messages/${messageId.value}/reactions/$emoji/@me").getOrThrow()
    }

    suspend fun removeUserReaction(
        channelId: Snowflake,
        messageId: Snowflake,
        emoji: String,
        userId: Snowflake
    ): Result<Unit> = runCatching {
        http.delete("/channels/${channelId.value}/messages/${messageId.value}/reactions/$emoji/$userId").getOrThrow()
    }

    suspend fun createThread(
        channelId: Snowflake,
        name: String,
        messageId: Snowflake? = null,
        autoArchiveDuration: Int = 60,
        type: Int? = null
    ): Result<Channel> = runCatching {
        val req = buildJsonObject {
            put("name", JsonPrimitive(name))
            put("auto_archive_duration", JsonPrimitive(autoArchiveDuration))
            type?.let { put("type", JsonPrimitive(it)) }
        }
        val body = req.toString()
        val endpoint = if (messageId != null) {
            "/channels/${channelId.value}/messages/${messageId.value}/threads"
        } else {
            "/channels/${channelId.value}/threads"
        }
        val json = http.post(endpoint, body).getOrThrow()
        http.json.decodeFromString<Channel>(json)
    }

    suspend fun joinThread(channelId: Snowflake): Result<Unit> = runCatching {
        http.put("/channels/${channelId.value}/thread-members/@me").getOrThrow()
    }

    suspend fun leaveThread(channelId: Snowflake): Result<Unit> = runCatching {
        http.delete("/channels/${channelId.value}/thread-members/@me").getOrThrow()
    }

    suspend fun listChannelInvites(channelId: Snowflake): Result<List<Map<String, Any>>> = runCatching {
        val json = http.get("/channels/${channelId.value}/invites").getOrThrow()
        @Suppress("UNCHECKED_CAST")
        http.json.decodeFromString<List<Map<String, Any>>>(json)
    }

    suspend fun createChannelInvite(
        channelId: Snowflake,
        maxAge: Int = 86400,
        maxUses: Int = 0,
        temporary: Boolean = false,
        unique: Boolean = false
    ): Result<Map<String, Any>> = runCatching {
        val body = buildJsonObject {
            put("max_age", JsonPrimitive(maxAge))
            put("max_uses", JsonPrimitive(maxUses))
            put("temporary", JsonPrimitive(temporary))
            put("unique", JsonPrimitive(unique))
        }.toString()
        val json = http.post("/channels/${channelId.value}/invites", body).getOrThrow()
        @Suppress("UNCHECKED_CAST")
        http.json.decodeFromString<Map<String, Any>>(json)
    }

    private fun componentToJson(component: Any): JsonElement {
        return JsonPrimitive(component.toString())
    }

    private fun embedToJson(embed: Any): JsonElement {
        return JsonPrimitive(embed.toString())
    }
}
