package com.discord.sdk.model

import kotlinx.serialization.Serializable

@Serializable
data class Channel(
    val id: Snowflake,
    val type: Int,
    val guildId: Snowflake? = null,
    val position: Int? = null,
    val name: String? = null,
    val topic: String? = null,
    val nsfw: Boolean = false,
    val lastMessageId: Snowflake? = null,
    val bitrate: Int? = null,
    val userLimit: Int? = null,
    val rateLimitPerUser: Int? = null,
    val recipients: List<User>? = null,
    val icon: String? = null,
    val ownerId: Snowflake? = null,
    val applicationId: Snowflake? = null,
    val parentId: Snowflake? = null,
    val lastPinTimestamp: String? = null,
    val rtcRegion: String? = null,
    val videoQualityMode: Int? = null,
    val messageCount: Int? = null,
    val memberCount: Int? = null,
    val threadMetadata: ThreadMetadata? = null,
    val member: ThreadMember? = null,
    val defaultAutoArchiveDuration: Int? = null,
    val permissions: String? = null,
    val flags: Int = 0
) {
    val isDM: Boolean get() = type == 1
    val isGroupDM: Boolean get() = type == 3
    val isGuildText: Boolean get() = type == 0
    val isGuildVoice: Boolean get() = type == 2
    val isGuildCategory: Boolean get() = type == 4
    val isGuildAnnouncement: Boolean get() = type == 5
    val isGuildForum: Boolean get() = type == 15
}

@Serializable
data class ThreadMetadata(
    val archived: Boolean = false,
    val autoArchiveDuration: Int,
    val archiveTimestamp: String,
    val locked: Boolean = false,
    val invitable: Boolean? = null,
    val createTimestamp: String? = null
)

@Serializable
data class ThreadMember(
    val id: Snowflake? = null,
    val userId: Snowflake? = null,
    val joinTimestamp: String,
    val flags: Int = 0
)

@Serializable
data class PermissionOverwrite(
    val id: Snowflake,
    val type: Int,
    val allow: String,
    val deny: String
)

enum class ChannelType(val value: Int) {
    GUILD_TEXT(0),
    DM(1),
    GUILD_VOICE(2),
    GROUP_DM(3),
    GUILD_CATEGORY(4),
    GUILD_ANNOUNCEMENT(5),
    ANNOUNCEMENT_THREAD(10),
    PUBLIC_THREAD(11),
    PRIVATE_THREAD(12),
    GUILD_STAGE_VOICE(13),
    GUILD_FORUM(15),
    GUILD_MEDIA(16);

    companion object {
        fun fromValue(value: Int): ChannelType =
            entries.firstOrNull { it.value == value } ?: GUILD_TEXT
    }
}
