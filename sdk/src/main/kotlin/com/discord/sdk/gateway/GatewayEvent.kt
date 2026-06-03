package com.discord.sdk.gateway

import kotlinx.serialization.Serializable

@Serializable
data class GatewayPayload(
    val op: Int,
    val d: Map<String, kotlinx.serialization.json.JsonElement>? = null,
    val s: Int? = null,
    val t: String? = null
)

@Serializable
data class IdentifyProperties(
    val os: String = "android",
    val browser: String = "DiscordSocialSDK",
    val device: String = "DiscordSocialSDK"
)

@Serializable
data class IdentifyData(
    val token: String,
    val properties: IdentifyProperties = IdentifyProperties(),
    val compress: Boolean = false,
    val largeThreshold: Int = 50,
    val guildSubscriptions: Boolean = false,
    val intents: Int
)

@Serializable
data class HeartbeatData(val op: Int = 1, val d: Int)

@Serializable
data class ResumeData(
    val token: String,
    val sessionId: String,
    val seq: Int
)

sealed class GatewayEvent {
    data class Ready(
        val version: Int,
        val user: com.discord.sdk.model.User,
        val sessionId: String,
        val guilds: List<Map<String, Any>>
    ) : GatewayEvent()

    data class MessageCreate(val message: com.discord.sdk.model.Message) : GatewayEvent()

    data class MessageUpdate(val message: com.discord.sdk.model.Message) : GatewayEvent()

    data class MessageDelete(val id: com.discord.sdk.model.Snowflake, val channelId: com.discord.sdk.model.Snowflake) : GatewayEvent()

    data class GuildCreate(val guild: com.discord.sdk.model.Guild) : GatewayEvent()

    data class GuildUpdate(val guild: com.discord.sdk.model.Guild) : GatewayEvent()

    data class GuildDelete(val id: com.discord.sdk.model.Snowflake) : GatewayEvent()

    data class GuildMemberAdd(val member: com.discord.sdk.model.GuildMember, val guildId: com.discord.sdk.model.Snowflake) : GatewayEvent()

    data class GuildMemberUpdate(val member: com.discord.sdk.model.GuildMember, val guildId: com.discord.sdk.model.Snowflake) : GatewayEvent()

    data class GuildMemberRemove(val user: com.discord.sdk.model.User, val guildId: com.discord.sdk.model.Snowflake) : GatewayEvent()

    data class PresenceUpdate(
        val user: com.discord.sdk.model.User,
        val guildId: com.discord.sdk.model.Snowflake?,
        val status: String,
        val activities: List<com.discord.sdk.model.Activity>
    ) : GatewayEvent()

    data class TypingStart(
        val channelId: com.discord.sdk.model.Snowflake,
        val userId: com.discord.sdk.model.Snowflake,
        val timestamp: Int
    ) : GatewayEvent()

    data class VoiceStateUpdate(val voiceState: com.discord.sdk.model.VoiceState) : GatewayEvent()

    data class ChannelCreate(val channel: com.discord.sdk.model.Channel) : GatewayEvent()

    data class ChannelUpdate(val channel: com.discord.sdk.model.Channel) : GatewayEvent()

    data class ChannelDelete(val channel: com.discord.sdk.model.Channel) : GatewayEvent()

    data class RelationshipAdd(val relationship: com.discord.sdk.model.Relationship) : GatewayEvent()

    data class RelationshipRemove(val relationship: com.discord.sdk.model.Relationship) : GatewayEvent()

    data class Unknown(val type: String, val data: String) : GatewayEvent()

    data object Resumed : GatewayEvent()

    data object Reconnect : GatewayEvent()

    data class InvalidSession(val resumable: Boolean) : GatewayEvent()
}
