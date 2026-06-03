package com.discord.sdk.model

import kotlinx.serialization.Serializable

@Serializable
data class Relationship(
    val id: Snowflake,
    val user: User,
    val type: Int
) {
    val isFriend: Boolean get() = type == 1
    val isBlocked: Boolean get() = type == 2
    val isIncomingRequest: Boolean get() = type == 3
    val isOutgoingRequest: Boolean get() = type == 4
}

enum class RelationshipType(val value: Int) {
    NONE(0),
    FRIEND(1),
    BLOCKED(2),
    PENDING_INCOMING(3),
    PENDING_OUTGOING(4);

    companion object {
        fun fromValue(value: Int): RelationshipType =
            entries.firstOrNull { it.value == value } ?: NONE
    }
}
