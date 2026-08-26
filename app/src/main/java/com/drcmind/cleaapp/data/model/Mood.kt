package com.drcmind.cleaapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Mood {
    @SerialName("VERY_BAD") VERY_BAD,
    @SerialName("BAD") BAD,
    @SerialName("NORMAL") NORMAL,
    @SerialName("GOOD") GOOD,
    @SerialName("VERY_GOOD") VERY_GOOD
}

