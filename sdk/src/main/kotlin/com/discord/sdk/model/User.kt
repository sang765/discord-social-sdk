package com.discord.sdk.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Snowflake,
    val username: String,
    val discriminator: String = "0",
    val globalName: String? = null,
    val avatar: String? = null,
    val bot: Boolean = false,
    val system: Boolean = false,
    val mfaEnabled: Boolean = false,
    val banner: String? = null,
    val accentColor: Int? = null,
    val locale: String? = null,
    val verified: Boolean? = null,
    val email: String? = null,
    val flags: Int = 0,
    val premiumType: Int = 0,
    val publicFlags: Int = 0,
    val avatarDecoration: String? = null
) {
    val displayName: String
        get() = globalName ?: username

    val avatarUrl: String
        get() = avatar?.let {
            "https://cdn.discordapp.com/avatars/${id.value}/$it.png"
        } ?: "https://cdn.discordapp.com/embed/avatars/0.png"
}

@Serializable
data class UserProfile(
    val user: User,
    val connectedAccounts: List<ConnectionAccount> = emptyList(),
    val premiumSince: String? = null,
    val premiumGuildSince: String? = null
)

@Serializable
data class ConnectionAccount(
    val id: String,
    val type: String,
    val name: String,
    val verified: Boolean = false
)
