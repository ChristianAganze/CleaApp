package com.drcmind.cleaapp.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object MoodSerializer : KSerializer<Mood> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Mood", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Mood) = encoder.encodeString(value.name)
    override fun deserialize(decoder: Decoder): Mood {
        val str = runCatching { decoder.decodeString().uppercase().trim() }.getOrDefault("NORMAL")
        return Mood.entries.firstOrNull { it.name.equals(str, ignoreCase = true) } ?: Mood.NORMAL
    }
}

@Serializable(with = MoodSerializer::class)
enum class Mood {
    VERY_BAD,
    BAD,
    NORMAL,
    GOOD,
    VERY_GOOD
}


