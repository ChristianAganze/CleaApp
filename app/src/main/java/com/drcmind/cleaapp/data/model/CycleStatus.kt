package com.drcmind.cleaapp.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object CycleStatusSerializer : KSerializer<CycleStatus> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("CycleStatus", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: CycleStatus) = encoder.encodeString(value.name)
    override fun deserialize(decoder: Decoder): CycleStatus {
        val str = runCatching { decoder.decodeString().uppercase().trim() }.getOrDefault("ACTIVE")
        return CycleStatus.entries.firstOrNull { it.name.equals(str, ignoreCase = true) } ?: CycleStatus.ACTIVE
    }
}

@Serializable(with = CycleStatusSerializer::class)
enum class CycleStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED
}




