package com.drcmind.cleaapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PredictionDto(
    @SerialName("average_cycle_length") val averageCycleLength: Int = 28,
    @SerialName("average_period_length") val averagePeriodLength: Int = 5,
    @SerialName("cycle_variation") val cycleVariation: Int = 0,
    @SerialName("predicted_period_start") val predictedPeriodStart: String = "",
    @SerialName("predicted_period_end") val predictedPeriodEnd: String = "",
    @SerialName("predicted_ovulation") val predictedOvulation: String = "",
    @SerialName("fertility_start") val fertilityStart: String = "",
    @SerialName("fertility_end") val fertilityEnd: String = "",
    @Serializable(with = FlexibleFloatSerializer::class)
    val confidence: Float? = 0f
)

