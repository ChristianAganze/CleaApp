package com.drcmind.cleaapp.data.remote.dto

import com.drcmind.cleaapp.data.model.FlowLevel
import com.drcmind.cleaapp.data.model.Mood
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CycleDayDto(
    @Serializable(with = FlexibleStringSerializer::class)
    val id: String = "",
    @Serializable(with = FlexibleStringSerializer::class)
    @SerialName("cycle_id") val cycleId: String = "",
    val date: String = "",
    val flow: FlowLevel = FlowLevel.MEDIUM,
    @Serializable(with = FlexibleIntSerializer::class)
    @SerialName("pain_level") val painLevel: Int? = null,
    val mood: Mood? = Mood.NORMAL,
    @Serializable(with = FlexibleFloatSerializer::class)
    val temperature: Float? = null,
    @Serializable(with = FlexibleFloatSerializer::class)
    val weight: Float? = null,
    val medications: String? = null,
    val notes: String? = null,
    val symptoms: List<SymptomDto> = emptyList()
)

