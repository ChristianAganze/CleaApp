package com.drcmind.cleaapp.data.remote.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull

object FlexibleStringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexibleString", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: String) = encoder.encodeString(value)
    override fun deserialize(decoder: Decoder): String {
        val jsonDecoder = decoder as? JsonDecoder ?: return runCatching { decoder.decodeString() }.getOrDefault("")
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonPrimitive -> element.content
            else -> element.toString()
        }
    }
}

object FlexibleBooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexibleBoolean", PrimitiveKind.BOOLEAN)
    override fun serialize(encoder: Encoder, value: Boolean) = encoder.encodeBoolean(value)
    override fun deserialize(decoder: Decoder): Boolean {
        val jsonDecoder = decoder as? JsonDecoder ?: return runCatching { decoder.decodeBoolean() }.getOrDefault(false)
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonPrimitive -> {
                element.booleanOrNull ?: (element.intOrNull == 1) ?: (element.content.equals("true", ignoreCase = true) || element.content == "1")
            }
            else -> false
        }
    }
}

object FlexibleIntSerializer : KSerializer<Int?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexibleInt", PrimitiveKind.INT)
    override fun serialize(encoder: Encoder, value: Int?) {
        if (value == null) encoder.encodeNull() else encoder.encodeInt(value)
    }
    override fun deserialize(decoder: Decoder): Int? {
        val jsonDecoder = decoder as? JsonDecoder ?: return runCatching { decoder.decodeInt() }.getOrNull()
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonPrimitive -> element.intOrNull ?: element.content.toIntOrNull()
            else -> null
        }
    }
}

object FlexibleFloatSerializer : KSerializer<Float?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexibleFloat", PrimitiveKind.FLOAT)
    override fun serialize(encoder: Encoder, value: Float?) {
        if (value == null) encoder.encodeNull() else encoder.encodeFloat(value)
    }
    override fun deserialize(decoder: Decoder): Float? {
        val jsonDecoder = decoder as? JsonDecoder ?: return runCatching { decoder.decodeFloat() }.getOrNull()
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonPrimitive -> element.floatOrNull ?: element.content.toFloatOrNull()
            else -> null
        }
    }
}
