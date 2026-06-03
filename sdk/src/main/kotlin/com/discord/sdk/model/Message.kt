package com.discord.sdk.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: Snowflake,
    val channelId: Snowflake,
    val author: User? = null,
    val content: String = "",
    val timestamp: String,
    val editedTimestamp: String? = null,
    val tts: Boolean = false,
    val mentionEveryone: Boolean = false,
    val mentions: List<User> = emptyList(),
    val mentionRoles: List<Snowflake> = emptyList(),
    val mentionChannels: List<ChannelMention> = emptyList(),
    val attachments: List<Attachment> = emptyList(),
    val embeds: List<Embed> = emptyList(),
    val reactions: List<Reaction> = emptyList(),
    val nonce: String? = null,
    val pinned: Boolean = false,
    val webhookId: Snowflake? = null,
    val type: Int = 0,
    val activity: MessageActivity? = null,
    val application: Application? = null,
    val applicationId: Snowflake? = null,
    val flags: Int = 0,
    val referencedMessage: Message? = null,
    val interaction: MessageInteraction? = null,
    val thread: Channel? = null,
    val components: List<Component> = emptyList(),
    val stickerItems: List<StickerItem> = emptyList(),
    val position: Int = 0
)

@Serializable
data class ChannelMention(
    val id: Snowflake,
    val guildId: Snowflake,
    val type: Int,
    val name: String
)

@Serializable
data class Attachment(
    val id: Snowflake,
    val filename: String,
    val description: String? = null,
    val contentType: String? = null,
    val size: Int,
    val url: String,
    val proxyUrl: String,
    val height: Int? = null,
    val width: Int? = null,
    val ephemeral: Boolean = false
)

@Serializable
data class Embed(
    val title: String? = null,
    val type: String? = null,
    val description: String? = null,
    val url: String? = null,
    val timestamp: String? = null,
    val color: Int? = null,
    val footer: EmbedFooter? = null,
    val image: EmbedImage? = null,
    val thumbnail: EmbedThumbnail? = null,
    val video: EmbedVideo? = null,
    val provider: EmbedProvider? = null,
    val author: EmbedAuthor? = null,
    val fields: List<EmbedField> = emptyList()
)

@Serializable
data class EmbedFooter(val text: String, val iconUrl: String? = null, val proxyIconUrl: String? = null)

@Serializable
data class EmbedImage(val url: String, val proxyUrl: String? = null, val height: Int? = null, val width: Int? = null)

@Serializable
data class EmbedThumbnail(val url: String, val proxyUrl: String? = null, val height: Int? = null, val width: Int? = null)

@Serializable
data class EmbedVideo(val url: String? = null, val proxyUrl: String? = null, val height: Int? = null, val width: Int? = null)

@Serializable
data class EmbedProvider(val name: String? = null, val url: String? = null)

@Serializable
data class EmbedAuthor(val name: String, val url: String? = null, val iconUrl: String? = null, val proxyIconUrl: String? = null)

@Serializable
data class EmbedField(val name: String, val value: String, val inline: Boolean = false)

@Serializable
data class Reaction(
    val count: Int,
    val me: Boolean = false,
    val emoji: PartialEmoji
)

@Serializable
data class PartialEmoji(
    val id: Snowflake? = null,
    val name: String? = null,
    val animated: Boolean = false
)

@Serializable
data class MessageActivity(val type: Int, val partyId: String? = null)

@Serializable
data class Application(
    val id: Snowflake,
    val name: String,
    val icon: String? = null,
    val description: String,
    val summary: String = ""
)

@Serializable
data class MessageInteraction(
    val id: Snowflake,
    val type: Int,
    val name: String,
    val user: User
)

@Serializable
data class StickerItem(val id: Snowflake, val name: String, val formatType: Int)

@Serializable
sealed class Component {
    @Serializable
    data class ActionRow(val components: List<Component>) : Component()

    @Serializable
    data class Button(
        val style: Int,
        val label: String? = null,
        val emoji: PartialEmoji? = null,
        val customId: String? = null,
        val url: String? = null,
        val disabled: Boolean = false
    ) : Component()
}

data class MessageCreateRequest(
    val content: String? = null,
    val embeds: List<Embed>? = null,
    val tts: Boolean = false,
    val allowedMentions: AllowedMentions? = null,
    val messageReference: MessageReference? = null,
    val components: List<Component>? = null,
    val stickerIds: List<Snowflake>? = null,
    val flags: Int = 0
)

data class AllowedMentions(
    val parse: List<String> = listOf("users", "roles"),
    val roles: List<Snowflake> = emptyList(),
    val users: List<Snowflake> = emptyList(),
    val repliedUser: Boolean = false
)

data class MessageReference(
    val messageId: Snowflake,
    val channelId: Snowflake? = null,
    val guildId: Snowflake? = null,
    val failIfNotExists: Boolean = true
)
