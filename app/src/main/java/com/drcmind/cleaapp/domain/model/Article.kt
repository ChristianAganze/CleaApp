package com.drcmind.cleaapp.domain.model

enum class ArticleCategory(val slug: String, val label: String) {
    SANTE_MENSTRUELLE("sante-menstruelle", "Santé menstruelle"),
    PLANNING_FAMILIAL("planning-familial", "Planning familial"),
    COUPLE("couple", "Couple"),
    LEADERSHIP("leadership", "Leadership"),
    SPIRITUALITE("spiritualite", "Spiritualité"),
    DEVELOPPEMENT_PERSONNEL("developpement-personnel", "Développement personnel");

    companion object {
        fun fromSlug(slug: String?): ArticleCategory {
            if (slug.isNullOrBlank()) return SANTE_MENSTRUELLE
            val cleaned = slug.lowercase().trim()
            return entries.find { 
                it.slug == cleaned || 
                it.name.equals(cleaned, ignoreCase = true) ||
                (cleaned.contains("menstru") && it == SANTE_MENSTRUELLE) ||
                (cleaned.contains("famill") && it == PLANNING_FAMILIAL) ||
                (cleaned.contains("couple") && it == COUPLE) ||
                (cleaned.contains("leader") && it == LEADERSHIP) ||
                (cleaned.contains("spirit") && it == SPIRITUALITE) ||
                ((cleaned.contains("dév") || cleaned.contains("dev") || cleaned.contains("personnel")) && it == DEVELOPPEMENT_PERSONNEL)
            } ?: SANTE_MENSTRUELLE
        }
    }
}

data class Article(
    val id: String,
    val title: String,
    val slug: String,
    val category: ArticleCategory,
    val categoryName: String,
    val summary: String,
    val content: String,
    val readingTimeMinutes: Int,
    val viewsCount: Int = 0,
    val authorName: String,
    val imageUrl: String?,
    val isFavorite: Boolean,
    val publishedAt: String?
)
