package com.discord.sdk.model

import kotlinx.serialization.Serializable

@Serializable
data class Presence(
    val user: User,
    val guildId: Snowflake? = null,
    val status: String = "offline",
    val activities: List<Activity> = emptyList(),
    val clientStatus: ClientStatus? = null
)

@Serializable
data class ClientStatus(
    val desktop: String? = null,
    val mobile: String? = null,
    val web: String? = null
)

@Serializable
data class Activity(
    val name: String,
    val type: Int = 0,
    val url: String? = null,
    val createdAt: Long = 0,
    val timestamps: ActivityTimestamps? = null,
    val applicationId: Snowflake? = null,
    val details: String? = null,
    val state: String? = null,
    val emoji: PartialEmoji? = null,
    val party: ActivityParty? = null,
    val assets: ActivityAssets? = null,
    val secrets: ActivitySecrets? = null,
    val instance: Boolean = false,
    val flags: Int = 0,
    val buttons: List<String> = emptyList()
)

@Serializable
data class ActivityTimestamps(val start: Long? = null, val end: Long? = null)

@Serializable
data class ActivityParty(val id: String? = null, val size: List<Int>? = null)

@Serializable
data class ActivityAssets(
    val largeImage: String? = null,
    val largeText: String? = null,
    val smallImage: String? = null,
    val smallText: String? = null
)

@Serializable
data class ActivitySecrets(
    val join: String? = null,
    val spectate: String? = null,
    val match: String? = null
)

enum class ActivityType(val value: Int) {
    GAME(0),
    STREAMING(1),
    LISTENING(2),
    WATCHING(3),
    CUSTOM(4),
    COMPETING(5);

    companion object {
        fun fromValue(value: Int): ActivityType =
            entries.firstOrNull { it.value == value } ?: GAME
    }
}

enum class StatusType {
    ONLINE, IDLE, DO_NOT_DISTURB, OFFLINE, INVISIBLE
}
