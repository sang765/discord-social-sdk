package com.discord.sdk.gateway

import com.discord.sdk.DiscordConfig
import com.discord.sdk.core.DiscordHttpClient
import com.discord.sdk.core.WebSocketClient
import com.discord.sdk.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.*

class GatewayClient internal constructor(
    private val config: DiscordConfig,
    private val httpClient: DiscordHttpClient
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var wsClient: WebSocketClient? = null
    private var sessionId: String? = null
    private var sequence: Int? = null
    private var heartbeatJob: Job? = null
    private var connected = false

    private val _events = MutableSharedFlow<GatewayEvent>(replay = 0, extraBufferCapacity = 64)
    val events: Flow<GatewayEvent> = _events.asSharedFlow()

    private val json = httpClient.json

    fun connect() {
        if (connected) return
        wsClient = WebSocketClient(
            httpClient = httpClient,
            url = config.gatewayUrl,
            scope = scope
        )
        wsClient?.let { ws ->
            ws.connect()
            scope.launch {
                ws.events.collect { raw ->
                    handleRaw(raw)
                }
            }
        }
    }

    fun disconnect() {
        connected = false
        heartbeatJob?.cancel()
        wsClient?.disconnect()
    }

    fun sendPresence(status: String = "online", activity: Activity? = null) {
        val payload = buildJsonObject {
            put("op", JsonPrimitive(GatewayOpcode.PRESENCE_UPDATE.value))
            put("d", buildJsonObject {
                put("since", JsonNull)
                put("activities", buildJsonArray {
                    if (activity != null) {
                        add(encodeActivity(activity))
                    }
                })
                put("status", JsonPrimitive(status))
                put("afk", JsonPrimitive(false))
            })
        }
        wsClient?.send(payload.toString())
    }

    fun sendRichPresence(activity: JsonObject, status: String = "online") {
        val payload = buildJsonObject {
            put("op", JsonPrimitive(GatewayOpcode.PRESENCE_UPDATE.value))
            put("d", buildJsonObject {
                put("since", JsonNull)
                put("activities", buildJsonArray { add(activity) })
                put("status", JsonPrimitive(status))
                put("afk", JsonPrimitive(false))
            })
        }
        wsClient?.send(payload.toString())
    }

    fun clearRichPresence() {
        val payload = buildJsonObject {
            put("op", JsonPrimitive(GatewayOpcode.PRESENCE_UPDATE.value))
            put("d", buildJsonObject {
                put("since", JsonNull)
                put("activities", buildJsonArray { })
                put("status", JsonPrimitive("online"))
                put("afk", JsonPrimitive(false))
            })
        }
        wsClient?.send(payload.toString())
    }

    private fun encodeActivity(activity: Activity): JsonElement {
        return buildJsonObject {
            put("name", JsonPrimitive(activity.name))
            put("type", JsonPrimitive(activity.type))
            activity.url?.let { put("url", JsonPrimitive(it)) }
            activity.state?.let { put("state", JsonPrimitive(it)) }
            activity.details?.let { put("details", JsonPrimitive(it)) }
            activity.timestamps?.let { ts ->
                put("timestamps", buildJsonObject {
                    ts.start?.let { put("start", JsonPrimitive(it)) }
                    ts.end?.let { put("end", JsonPrimitive(it)) }
                })
            }
            activity.assets?.let { a ->
                put("assets", buildJsonObject {
                    a.largeImage?.let { put("large_image", JsonPrimitive(it)) }
                    a.largeText?.let { put("large_text", JsonPrimitive(it)) }
                    a.smallImage?.let { put("small_image", JsonPrimitive(it)) }
                    a.smallText?.let { put("small_text", JsonPrimitive(it)) }
                })
            }
            activity.party?.let { p ->
                put("party", buildJsonObject {
                    p.id?.let { put("id", JsonPrimitive(it)) }
                    p.size?.let { put("size", buildJsonArray { it.forEach { add(JsonPrimitive(it)) } }) }
                })
            }
            activity.secrets?.let { s ->
                put("secrets", buildJsonObject {
                    s.join?.let { put("join", JsonPrimitive(it)) }
                    s.spectate?.let { put("spectate", JsonPrimitive(it)) }
                    s.match?.let { put("match", JsonPrimitive(it)) }
                })
            }
            activity.instance?.let { put("instance", JsonPrimitive(it)) }
            activity.flags?.let { put("flags", JsonPrimitive(it)) }
            activity.buttons?.let { btns ->
                put("buttons", buildJsonArray { btns.forEach { add(JsonPrimitive(it)) } })
            }
        }
    }

    internal fun sendRaw(payload: String): Boolean = wsClient?.send(payload) ?: false

    private suspend fun handleRaw(raw: String) {
        try {
            val payload = json.decodeFromString<JsonObject>(raw)
            val op = payload["op"]?.jsonPrimitive?.int ?: return
            val data = payload["d"]
            val seq = payload["s"]?.jsonPrimitive?.intOrNull
            val type = payload["t"]?.jsonPrimitive?.contentOrNull

            if (seq != null) sequence = seq

            when (GatewayOpcode.entries.firstOrNull { it.value == op }) {
                GatewayOpcode.HELLO -> handleHello(data)
                GatewayOpcode.DISPATCH -> handleDispatch(type, data, raw)
                GatewayOpcode.HEARTBEAT -> sendHeartbeat()
                GatewayOpcode.HEARTBEAT_ACK -> {}
                GatewayOpcode.RECONNECT -> handleReconnect()
                GatewayOpcode.INVALID_SESSION -> handleInvalidSession(data)
                else -> {}
            }
        } catch (e: Exception) {
            // skip malformed payloads
        }
    }

    private suspend fun handleHello(data: JsonElement?) {
        val interval = data?.jsonObject?.get("heartbeat_interval")?.jsonPrimitive?.int ?: 41250
        startHeartbeating(interval)
        identify()
    }

    private fun startHeartbeating(intervalMs: Int) {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (isActive) {
                sendHeartbeat()
                delay(intervalMs.toLong())
            }
        }
    }

    private fun sendHeartbeat() {
        val payload = buildJsonObject {
            put("op", JsonPrimitive(GatewayOpcode.HEARTBEAT.value))
            put("d", sequence?.let { JsonPrimitive(it) } ?: JsonNull)
        }
        wsClient?.send(payload.toString())
    }

    private fun identify() {
        val token = config.token ?: return
        val payload = buildJsonObject {
            put("op", JsonPrimitive(GatewayOpcode.IDENTIFY.value))
            put("d", buildJsonObject {
                put("token", JsonPrimitive(token))
                put("properties", buildJsonObject {
                    put("os", JsonPrimitive("android"))
                    put("browser", JsonPrimitive("ArchiveTune"))
                    put("device", JsonPrimitive("ArchiveTune"))
                })
                put("compress", JsonPrimitive(false))
                put("large_threshold", JsonPrimitive(50))
                if (config.isBot) {
                    put("intents", JsonPrimitive(GatewayIntent.calculateIntents(config.intents)))
                    put("guild_subscriptions", JsonPrimitive(false))
                } else {
                    put("capabilities", JsonPrimitive(16381))
                }
            })
        }
        wsClient?.send(payload.toString())
    }

    private suspend fun handleDispatch(type: String?, data: JsonElement?, raw: String) {
        if (type == null || data == null) return

        val event = when (type) {
            "READY" -> {
                val readyObj = data.jsonObject
                sessionId = readyObj["session_id"]?.jsonPrimitive?.content
                connected = true
                GatewayEvent.Ready(
                    version = readyObj["v"]?.jsonPrimitive?.int ?: 10,
                    user = json.decodeFromJsonElement(readyObj["user"]!!),
                    sessionId = sessionId ?: "",
                    guilds = emptyList()
                )
            }
            "RESUMED" -> {
                connected = true
                GatewayEvent.Resumed
            }
            "MESSAGE_CREATE" -> {
                GatewayEvent.MessageCreate(json.decodeFromJsonElement(data))
            }
            "MESSAGE_UPDATE" -> {
                GatewayEvent.MessageUpdate(json.decodeFromJsonElement(data))
            }
            "MESSAGE_DELETE" -> {
                val obj = data.jsonObject
                GatewayEvent.MessageDelete(
                    id = Snowflake(obj["id"]?.jsonPrimitive?.content ?: ""),
                    channelId = Snowflake(obj["channel_id"]?.jsonPrimitive?.content ?: "")
                )
            }
            "GUILD_CREATE" -> {
                GatewayEvent.GuildCreate(json.decodeFromJsonElement(data))
            }
            "GUILD_UPDATE" -> {
                GatewayEvent.GuildUpdate(json.decodeFromJsonElement(data))
            }
            "GUILD_DELETE" -> {
                val obj = data.jsonObject
                GatewayEvent.GuildDelete(
                    id = Snowflake(obj["id"]?.jsonPrimitive?.content ?: "")
                )
            }
            "GUILD_MEMBER_ADD" -> {
                GatewayEvent.GuildMemberAdd(
                    member = json.decodeFromJsonElement(data),
                    guildId = Snowflake(data.jsonObject["guild_id"]?.jsonPrimitive?.content ?: "")
                )
            }
            "GUILD_MEMBER_UPDATE" -> {
                GatewayEvent.GuildMemberUpdate(
                    member = json.decodeFromJsonElement(data),
                    guildId = Snowflake(data.jsonObject["guild_id"]?.jsonPrimitive?.content ?: "")
                )
            }
            "GUILD_MEMBER_REMOVE" -> {
                val obj = data.jsonObject
                GatewayEvent.GuildMemberRemove(
                    user = json.decodeFromJsonElement(obj["user"]!!),
                    guildId = Snowflake(obj["guild_id"]?.jsonPrimitive?.content ?: "")
                )
            }
            "PRESENCE_UPDATE" -> {
                val obj = data.jsonObject
                GatewayEvent.PresenceUpdate(
                    user = json.decodeFromJsonElement(obj["user"]!!),
                    guildId = obj["guild_id"]?.jsonPrimitive?.content?.let { Snowflake(it) },
                    status = obj["status"]?.jsonPrimitive?.content ?: "offline",
                    activities = obj["activities"]?.let {
                        json.decodeFromJsonElement<List<Activity>>(it)
                    } ?: emptyList()
                )
            }
            "TYPING_START" -> {
                val obj = data.jsonObject
                GatewayEvent.TypingStart(
                    channelId = Snowflake(obj["channel_id"]?.jsonPrimitive?.content ?: ""),
                    userId = Snowflake(obj["user_id"]?.jsonPrimitive?.content ?: ""),
                    timestamp = obj["timestamp"]?.jsonPrimitive?.int ?: 0
                )
            }
            "VOICE_STATE_UPDATE" -> {
                GatewayEvent.VoiceStateUpdate(json.decodeFromJsonElement(data))
            }
            "CHANNEL_CREATE" -> {
                GatewayEvent.ChannelCreate(json.decodeFromJsonElement(data))
            }
            "CHANNEL_UPDATE" -> {
                GatewayEvent.ChannelUpdate(json.decodeFromJsonElement(data))
            }
            "CHANNEL_DELETE" -> {
                GatewayEvent.ChannelDelete(json.decodeFromJsonElement(data))
            }
            "RELATIONSHIP_ADD" -> {
                GatewayEvent.RelationshipAdd(json.decodeFromJsonElement(data))
            }
            "RELATIONSHIP_REMOVE" -> {
                GatewayEvent.RelationshipRemove(json.decodeFromJsonElement(data))
            }
            "RECONNECT" -> GatewayEvent.Reconnect
            else -> GatewayEvent.Unknown(type, data.toString())
        }

        _events.emit(event)
    }

    private fun handleReconnect() {
        connected = false
        heartbeatJob?.cancel()
        wsClient?.disconnect()
        scope.launch {
            delay(2000)
            connect()
        }
        scope.launch {
            _events.emit(GatewayEvent.Reconnect)
        }
    }

    private suspend fun handleInvalidSession(data: JsonElement?) {
        val resumable = data?.jsonPrimitive?.booleanOrNull ?: false
        _events.emit(GatewayEvent.InvalidSession(resumable))
        if (!resumable) {
            sessionId = null
            sequence = null
        }
        delay(2000)
        connect()
    }

    fun isConnected(): Boolean = connected

    fun destroy() {
        disconnect()
        scope.cancel()
    }
}
