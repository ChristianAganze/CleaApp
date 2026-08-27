package com.drcmind.cleaapp.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FlowLevelSerializer : KSerializer<FlowLevel> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlowLevel", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: FlowLevel) = encoder.encodeString(value.name)
    override fun deserialize(decoder: Decoder): FlowLevel {
        val str = runCatching { decoder.decodeString().uppercase().trim() }.getOrDefault("MEDIUM")
        return FlowLevel.entries.firstOrNull { it.name.equals(str, ignoreCase = true) } ?: FlowLevel.MEDIUM
    }
}

@Serializable(with = FlowLevelSerializer::class)
enum class FlowLevel {
    NONE,
    SPOTTING,
    LIGHT,
    MEDIUM,
    HEAVY
}
