package com.discord.sdk.api

import com.discord.sdk.core.DiscordHttpClient
import com.discord.sdk.model.*

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
        val req = buildMap<String, Any> {
            name?.let { put("name", it) }
            topic?.let { put("topic", it) }
            position?.let { put("position", it) }
            nsfw?.let { put("nsfw", it) }
            rateLimitPerUser?.let { put("rate_limit_per_user", it) }
            bitrate?.let { put("bitrate", it) }
            userLimit?.let { put("user_limit", it) }
            parentId?.let { put("parent_id", it.value) }
        }
        val body = http.json.encodeToString(req)
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
        val req = buildMap<String, Any?> {
            request.content?.let { put("content", it) }
            request.tts?.let { put("tts", it) }
            request.components?.let { put("components", it) }
            request.stickerIds?.let { put("sticker_ids", it.map { s -> s.value }) }
            request.flags?.let { put("flags", it) }
            request.embeds?.let { put("embeds", it) }
            request.allowedMentions?.let {
                put("allowed_mentions", mapOf(
                    "parse" to it.parse,
                    "roles" to it.roles.map { r -> r.value },
                    "users" to it.users.map { u -> u.value },
                    "replied_user" to it.repliedUser
                ))
            }
            request.messageReference?.let {
                put("message_reference", mapOf(
                    "message_id" to it.messageId.value,
                    "channel_id" to it.channelId?.value,
                    "guild_id" to it.guildId?.value,
                    "fail_if_not_exists" to it.failIfNotExists
                ))
            }
        }.filterValues { it != null }
        val body = http.json.encodeToString(req)
        val json = http.post("/channels/${channelId.value}/messages", body).getOrThrow()
        http.json.decodeFromString<Message>(json)
    }

    suspend fun deleteMessage(channelId: Snowflake, messageId: Snowflake): Result<Unit> = runCatching {
        http.delete("/channels/${channelId.value}/messages/${messageId.value}").getOrThrow()
        Unit
    }

    suspend fun bulkDeleteMessages(channelId: Snowflake, messageIds: List<Snowflake>): Result<Unit> = runCatching {
        val body = http.json.encodeToString(mapOf("messages" to messageIds.map { it.value }))
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
        val req = mutableMapOf(
            "name" to name,
            "auto_archive_duration" to autoArchiveDuration.toString()
        )
        type?.let { req["type"] = it.toString() }
        val body = http.json.encodeToString(req)
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
        val req = mapOf(
            "max_age" to maxAge,
            "max_uses" to maxUses,
            "temporary" to temporary,
            "unique" to unique
        )
        val body = http.json.encodeToString(req)
        val json = http.post("/channels/${channelId.value}/invites", body).getOrThrow()
        @Suppress("UNCHECKED_CAST")
        http.json.decodeFromString<Map<String, Any>>(json)
    }
}
