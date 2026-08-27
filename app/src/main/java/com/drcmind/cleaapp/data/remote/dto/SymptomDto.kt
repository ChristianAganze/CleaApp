package com.drcmind.cleaapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SymptomDto(
    @Serializable(with = FlexibleStringSerializer::class)
    val id: String = "",
    val name: String = "",
    val slug: String = "",
    val description: String? = null,
    val icon: String? = null,
    @Serializable(with = FlexibleBooleanSerializer::class)
    @SerialName("is_active") val isActive: Boolean = true
)


