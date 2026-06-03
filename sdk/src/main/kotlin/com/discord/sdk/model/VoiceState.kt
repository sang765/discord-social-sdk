package com.discord.sdk.model

import kotlinx.serialization.Serializable

@Serializable
data class VoiceState(
    val guildId: Snowflake? = null,
    val channelId: Snowflake? = null,
    val userId: Snowflake,
    val member: GuildMember? = null,
    val sessionId: String,
    val deaf: Boolean = false,
    val mute: Boolean = false,
    val selfDeaf: Boolean = false,
    val selfMute: Boolean = false,
    val selfStream: Boolean = false,
    val selfVideo: Boolean = false,
    val suppress: Boolean = false,
    val requestToSpeakTimestamp: String? = null
)

@Serializable
data class VoiceRegion(
    val id: String,
    val name: String,
    val optimal: Boolean = false,
    val deprecated: Boolean = false,
    val custom: Boolean = false
)
