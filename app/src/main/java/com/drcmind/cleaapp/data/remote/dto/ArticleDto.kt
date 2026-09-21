package com.drcmind.cleaapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticleListResponseDto(
    val data: List<ArticleDto> = emptyList(),
    val meta: PaginationMetaDto? = null
)

@Serializable
data class ArticleDto(
    val id: String,
    val title: String,
    val category: String, // "menstruation", "family_planning", "couple", "leadership", "wellbeing", "spirituality"
    val summary: String,
    val content: String,
    @SerialName("reading_time_minutes") val readingTimeMinutes: Int = 4,
    @SerialName("author_name") val authorName: String? = "Équipe Médicale Cléa",
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("is_favorite") val isFavorite: Boolean = false,
    @SerialName("published_at") val publishedAt: String? = null
)
