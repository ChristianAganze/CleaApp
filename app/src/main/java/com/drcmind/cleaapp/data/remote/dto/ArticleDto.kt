package com.drcmind.cleaapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class CategoryDto(
    val id: String = "",
    val name: String,
    val slug: String,
    val description: String? = null,
    val icon: String? = null,
    val position: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("published_articles_count") val publishedArticlesCount: Int? = null
)

@Serializable
data class ArticleListResponseDto(
    val data: List<ArticleDto> = emptyList(),
    val meta: PaginationMetaDto? = null
)

@Serializable
data class ArticleDto(
    val id: String,
    val title: String,
    val slug: String? = null,
    val excerpt: String? = null,
    val summary: String? = null,
    @SerialName("cover_image_url") val coverImageUrl: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("published_at") val publishedAt: String? = null,
    @SerialName("reading_time_minutes") val readingTimeMinutes: Int = 4,
    @SerialName("views_count") val viewsCount: Int = 0,
    @SerialName("is_favorite") val isFavorite: Boolean = false,
    @SerialName("body_markdown") val bodyMarkdown: String? = null,
    val content: String? = null,
    @SerialName("author_name") val authorName: String? = "Équipe Médicale Cléa",
    val category: JsonElement? = null,
    val related: List<ArticleDto> = emptyList(),
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
) {
    fun getCategorySlug(): String {
        return when (val elem = category) {
            is JsonObject -> elem["slug"]?.jsonPrimitive?.content ?: elem["name"]?.jsonPrimitive?.content ?: "sante-menstruelle"
            else -> elem?.jsonPrimitive?.content ?: "sante-menstruelle"
        }
    }

    fun getCategoryName(): String {
        return when (val elem = category) {
            is JsonObject -> elem["name"]?.jsonPrimitive?.content ?: elem["slug"]?.jsonPrimitive?.content ?: "Santé menstruelle"
            else -> elem?.jsonPrimitive?.content ?: "Santé menstruelle"
        }
    }

    fun getEffectiveSlug(): String {
        return slug?.takeIf { it.isNotBlank() } ?: id
    }

    fun getEffectiveSummary(): String {
        return excerpt?.takeIf { it.isNotBlank() } ?: summary ?: bodyMarkdown?.take(180) ?: content?.take(180) ?: ""
    }

    fun getEffectiveContent(): String {
        return bodyMarkdown?.takeIf { it.isNotBlank() } ?: content ?: excerpt ?: summary ?: ""
    }

    fun getEffectiveImageUrl(): String? {
        return coverImageUrl?.takeIf { it.isNotBlank() } ?: imageUrl
    }
}
