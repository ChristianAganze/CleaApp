package com.drcmind.cleaapp.domain.model

data class HomeDashboard(
    val date: String,
    val cleaMessage: CleaMessage?,
    val nextPeriod: NextPeriodPrediction?,
    val todayTasks: TodayTasksSummary,
    val recommendedArticle: RecommendedArticle?,
    val unreadNotificationsCount: Int
)

data class CleaMessage(
    val id: String,
    val title: String,
    val body: String
)

data class NextPeriodPrediction(
    val startDate: String,
    val endDate: String?,
    val ovulationDate: String?,
    val daysUntil: Int,
    val confidence: Float
)

data class TodayTasksSummary(
    val items: List<HomeTaskItem>,
    val total: Int,
    val done: Int
)

data class HomeTaskItem(
    val id: String,
    val kind: String, // "task" or "reminder"
    val title: String,
    val category: String, // "work", "family", "spirituality", "personal"
    val dueAt: String?,
    val isDone: Boolean
)

data class RecommendedArticle(
    val id: String,
    val title: String,
    val slug: String,
    val excerpt: String,
    val coverImageUrl: String?,
    val readingTimeMinutes: Int,
    val categoryName: String
)
