package com.discord.sdk.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = SnowflakeSerializer::class)
data class Snowflake(val value: String) {
    companion object {
        fun fromLong(id: Long) = Snowflake(id.toString())
    }
}

object SnowflakeSerializer : KSerializer<Snowflake> {
    override val descriptor = PrimitiveSerialDescriptor("Snowflake", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Snowflake) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): Snowflake {
        val str = decoder.decodeString()
        return Snowflake(str)
    }
}

fun String.toSnowflake() = Snowflake(this)
fun Long.toSnowflake() = Snowflake.fromLong(this)
