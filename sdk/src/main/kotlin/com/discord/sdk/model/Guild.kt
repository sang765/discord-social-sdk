package com.discord.sdk.model

import kotlinx.serialization.Serializable

@Serializable
data class Guild(
    val id: Snowflake,
    val name: String,
    val icon: String? = null,
    val splash: String? = null,
    val discoverySplash: String? = null,
    val ownerId: Snowflake,
    val afkChannelId: Snowflake? = null,
    val afkTimeout: Int = 0,
    val verificationLevel: Int = 0,
    val defaultMessageNotifications: Int = 0,
    val explicitContentFilter: Int = 0,
    val roles: List<Role> = emptyList(),
    val emojis: List<Emoji> = emptyList(),
    val features: List<String> = emptyList(),
    val mfaLevel: Int = 0,
    val applicationId: Snowflake? = null,
    val systemChannelId: Snowflake? = null,
    val rulesChannelId: Snowflake? = null,
    val memberCount: Int = 0,
    val premiumTier: Int = 0,
    val premiumSubscriberCount: Int = 0,
    val preferredLocale: String = "en-US",
    val description: String? = null,
    val maxMembers: Int = 0,
    val maxVideoChannelUsers: Int = 0,
    val approximateMemberCount: Int? = null,
    val approximatePresenceCount: Int? = null
) {
    val iconUrl: String?
        get() = icon?.let {
            "https://cdn.discordapp.com/icons/${id.value}/$it.png"
        }
}

@Serializable
data class GuildMember(
    val user: User? = null,
    val nick: String? = null,
    val avatar: String? = null,
    val roles: List<Snowflake> = emptyList(),
    val joinedAt: String,
    val premiumSince: String? = null,
    val deaf: Boolean = false,
    val mute: Boolean = false,
    val flags: Int = 0
) {
    val effectiveName: String
        get() = nick ?: user?.displayName ?: "Unknown"
}

@Serializable
data class Role(
    val id: Snowflake,
    val name: String,
    val color: Int = 0,
    val hoist: Boolean = false,
    val icon: String? = null,
    val unicodeEmoji: String? = null,
    val position: Int = 0,
    val permissions: String = "0",
    val managed: Boolean = false,
    val mentionable: Boolean = false,
    val tags: RoleTags? = null
)

@Serializable
data class RoleTags(
    val botId: Snowflake? = null,
    val integrationId: Snowflake? = null,
    val premiumSubscriber: Boolean? = null
)

@Serializable
data class Emoji(
    val id: Snowflake? = null,
    val name: String? = null,
    val roles: List<Snowflake> = emptyList(),
    val user: User? = null,
    val requireColons: Boolean = true,
    val managed: Boolean = false,
    val animated: Boolean = false,
    val available: Boolean = true
)
