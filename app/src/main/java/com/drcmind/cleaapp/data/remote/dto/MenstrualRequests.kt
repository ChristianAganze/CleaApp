package com.drcmind.cleaapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateCycleRequest(
    @SerialName("start_date") val startDate: String,
    val notes: String? = null
)

@Serializable
data class CompleteCycleRequest(
    @SerialName("end_date") val endDate: String,
    @SerialName("cycle_length") val cycleLength: Int,
    @SerialName("period_length") val periodLength: Int
)

@Serializable
data class CreateCycleDayRequest(
    val date: String,
    val flow: String,
    @SerialName("pain_level") val painLevel: Int? = null,
    val mood: String? = null,
    val temperature: Float? = null,
    val weight: Float? = null,
    val medications: String? = null,
    val notes: String? = null,
    @SerialName("symptom_ids") val symptomIds: List<String>? = null
)

@Serializable
data class UpdateCycleDayRequest(
    val flow: String? = null,
    @SerialName("pain_level") val painLevel: Int? = null,
    val mood: String? = null,
    val temperature: Float? = null,
    val weight: Float? = null,
    val medications: String? = null,
    val notes: String? = null
)

@Serializable
data class SymptomItemPayload(
    val id: String,
    val severity: Int = 1
)

@Serializable
data class AttachSymptomsRequest(
    val symptoms: List<SymptomItemPayload>? = null,
    @SerialName("symptom_ids") val symptomIds: List<String>? = null
)
