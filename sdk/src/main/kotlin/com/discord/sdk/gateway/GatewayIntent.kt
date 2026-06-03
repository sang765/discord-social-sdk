package com.discord.sdk.gateway

enum class GatewayIntent(val value: Int) {
    GUILDS(1 shl 0),
    GUILD_MEMBERS(1 shl 1),
    GUILD_MODERATION(1 shl 2),
    GUILD_BANS(1 shl 3),
    GUILD_EMOJIS_AND_STICKERS(1 shl 4),
    GUILD_INTEGRATIONS(1 shl 5),
    GUILD_WEBHOOKS(1 shl 6),
    GUILD_INVITES(1 shl 7),
    GUILD_VOICE_STATES(1 shl 8),
    GUILD_PRESENCES(1 shl 9),
    GUILD_MESSAGES(1 shl 10),
    GUILD_MESSAGE_REACTIONS(1 shl 11),
    GUILD_MESSAGE_TYPING(1 shl 12),
    DIRECT_MESSAGES(1 shl 13),
    DIRECT_MESSAGE_REACTIONS(1 shl 14),
    DIRECT_MESSAGE_TYPING(1 shl 15),
    MESSAGE_CONTENT(1 shl 15),
    GUILD_SCHEDULED_EVENTS(1 shl 16);

    companion object {
        fun calculateIntents(intents: Set<GatewayIntent>): Int =
            intents.fold(0) { acc, intent -> acc or intent.value }
    }
}
