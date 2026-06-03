package com.discord.sdk

import com.discord.sdk.core.DiscordHttpClient
import com.discord.sdk.model.*
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class ModelSerializationTest {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun testUserSerialization() {
        val userJson = """
            {
                "id": "123456789",
                "username": "testuser",
                "discriminator": "0001",
                "global_name": "Test User",
                "avatar": null,
                "bot": false
            }
        """
        val user = json.decodeFromString<User>(userJson)
        assertEquals(Snowflake("123456789"), user.id)
        assertEquals("testuser", user.username)
        assertEquals("Test User", user.displayName)
    }

    @Test
    fun testMessageSerialization() {
        val msgJson = """
            {
                "id": "987654321",
                "channel_id": "111222333",
                "content": "Hello, world!",
                "timestamp": "2024-01-01T00:00:00Z",
                "author": {
                    "id": "123456789",
                    "username": "author",
                    "discriminator": "0000"
                },
                "pinned": false,
                "type": 0,
                "flags": 0
            }
        """
        val msg = json.decodeFromString<Message>(msgJson)
        assertEquals(Snowflake("987654321"), msg.id)
        assertEquals("Hello, world!", msg.content)
        assertNotNull(msg.author)
        assertEquals("author", msg.author?.username)
    }

    @Test
    fun testGuildSerialization() {
        val guildJson = """
            {
                "id": "555666777",
                "name": "Test Guild",
                "owner_id": "123456789",
                "verification_level": 0,
                "default_message_notifications": 0,
                "explicit_content_filter": 0,
                "features": [],
                "mfa_level": 0,
                "preferred_locale": "en-US"
            }
        """
        val guild = json.decodeFromString<Guild>(guildJson)
        assertEquals(Snowflake("555666777"), guild.id)
        assertEquals("Test Guild", guild.name)
        assertEquals(Snowflake("123456789"), guild.ownerId)
    }

    @Test
    fun testChannelSerialization() {
        val channelJson = """
            {
                "id": "111222333",
                "type": 0,
                "guild_id": "555666777",
                "name": "general",
                "position": 0,
                "nsfw": false,
                "flags": 0
            }
        """
        val channel = json.decodeFromString<Channel>(channelJson)
        assertEquals(Snowflake("111222333"), channel.id)
        assertEquals("general", channel.name)
        assertTrue(channel.isGuildText)
    }

    @Test
    fun testRelationshipSerialization() {
        val relJson = """
            {
                "id": "1",
                "user": {
                    "id": "123",
                    "username": "friend",
                    "discriminator": "0000"
                },
                "type": 1
            }
        """
        val rel = json.decodeFromString<Relationship>(relJson)
        assertTrue(rel.isFriend)
        assertEquals("friend", rel.user.username)
    }

    @Test
    fun testSnowflakeFromString() {
        val sf = "123456789".toSnowflake()
        assertEquals(Snowflake("123456789"), sf)
        assertEquals("123456789", sf.value)
    }

    @Test
    fun testEmbedSerialization() {
        val embedJson = """
            {
                "title": "Test Embed",
                "description": "This is a test embed",
                "color": 16711680,
                "fields": [
                    {"name": "Field 1", "value": "Value 1", "inline": true}
                ]
            }
        """
        val embed = json.decodeFromString<Embed>(embedJson)
        assertEquals("Test Embed", embed.title)
        assertEquals(16711680, embed.color)
        assertEquals(1, embed.fields.size)
    }

    @Test
    fun testPartialEmoji() {
        val emojiJson = """{"id": null, "name": "🔥", "animated": false}"""
        val emoji = json.decodeFromString<PartialEmoji>(emojiJson)
        assertNull(emoji.id)
        assertEquals("🔥", emoji.name)
    }

    @Test
    fun testVoiceState() {
        val vsJson = """
            {
                "guild_id": "555666777",
                "channel_id": "111222333",
                "user_id": "123456789",
                "session_id": "abc123",
                "deaf": false,
                "mute": false,
                "self_deaf": false,
                "self_mute": false,
                "self_stream": false,
                "self_video": false,
                "suppress": false
            }
        """
        val vs = json.decodeFromString<VoiceState>(vsJson)
        assertEquals(Snowflake("123456789"), vs.userId)
        assertEquals("abc123", vs.sessionId)
    }

    @Test
    fun testPresence() {
        val presenceJson = """
            {
                "user": {
                    "id": "123",
                    "username": "user",
                    "discriminator": "0000"
                },
                "status": "online",
                "activities": [
                    {
                        "name": "Playing a game",
                        "type": 0,
                        "created_at": 1700000000
                    }
                ],
                "client_status": {
                    "desktop": "online",
                    "mobile": "idle"
                }
            }
        """
        val presence = json.decodeFromString<Presence>(presenceJson)
        assertEquals("online", presence.status)
        assertEquals(1, presence.activities.size)
    }
}
