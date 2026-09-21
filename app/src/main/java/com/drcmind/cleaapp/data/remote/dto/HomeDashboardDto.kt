package com.drcmind.cleaapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HomeDashboardDto(
    val date: String? = null,
    @SerialName("clea_message") val cleaMessage: CleaMessageDto? = null,
    @SerialName("next_period") val nextPeriod: NextPeriodDto? = null,
    @SerialName("today_tasks") val todayTasks: TodayTasksDto? = null,
    @SerialName("recommended_article") val recommendedArticle: RecommendedArticleDto? = null,
    @SerialName("unread_notifications_count") val unreadNotificationsCount: Int = 0
)

@Serializable
data class CleaMessageDto(
    val id: String? = null,
    val title: String? = null,
    val body: String
)

@Serializable
data class NextPeriodDto(
    @SerialName("predicted_period_start") val predictedPeriodStart: String? = null,
    @SerialName("predicted_period_end") val predictedPeriodEnd: String? = null,
    @SerialName("predicted_ovulation") val predictedOvulation: String? = null,
    @SerialName("days_until") val daysUntil: Int? = null,
    val confidence: Float? = null
)

@Serializable
data class TodayTasksDto(
    val items: List<TodayTaskDto> = emptyList(),
    val total: Int = 0,
    val done: Int = 0
)

@Serializable
data class TodayTaskDto(
    val id: String,
    val kind: String = "task",
    val title: String,
    val category: String? = "personal",
    @SerialName("due_at") val dueAt: String? = null,
    val status: String = "pending"
)

@Serializable
data class RecommendedArticleDto(
    val id: String,
    val title: String,
    val slug: String? = null,
    val excerpt: String? = null,
    @SerialName("cover_image_url") val coverImageUrl: String? = null,
    @SerialName("reading_time_minutes") val readingTimeMinutes: Int = 3,
    val category: ArticleCategoryDto? = null
)

@Serializable
data class ArticleCategoryDto(
    val name: String,
    val slug: String? = null
)
