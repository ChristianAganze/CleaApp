package com.drcmind.cleaapp.domain.model

enum class ArticleCategory(val apiValue: String, val label: String) {
    MENSTRUATION("menstruation", "Santé menstruelle"),
    FAMILY_PLANNING("family_planning", "Planification familiale"),
    COUPLE("couple", "Vie de couple"),
    LEADERSHIP("leadership", "Leadership & Carrière"),
    WELLBEING("wellbeing", "Bien-être & Corps"),
    SPIRITUALITY("spirituality", "Spiritualité & Méditation");

    companion object {
        fun fromApi(value: String): ArticleCategory =
            entries.find { it.apiValue.equals(value, ignoreCase = true) } ?: MENSTRUATION
    }
}

data class Article(
    val id: String,
    val title: String,
    val category: ArticleCategory,
    val summary: String,
    val content: String,
    val readingTimeMinutes: Int,
    val authorName: String,
    val imageUrl: String?,
    val isFavorite: Boolean,
    val publishedAt: String?
)
