package com.drcmind.cleaapp.data.mapper

import com.drcmind.cleaapp.data.local.room.entity.ArticleEntity
import com.drcmind.cleaapp.data.remote.dto.ArticleDto
import com.drcmind.cleaapp.domain.model.Article
import com.drcmind.cleaapp.domain.model.ArticleCategory

fun ArticleDto.toEntity(): ArticleEntity {
    val categorySlug = getCategorySlug()
    val categoryLabel = getCategoryName()
    return ArticleEntity(
        id = id,
        title = title,
        slug = getEffectiveSlug(),
        category = categorySlug,
        categoryName = categoryLabel,
        summary = getEffectiveSummary(),
        content = getEffectiveContent(),
        readingTimeMinutes = readingTimeMinutes,
        viewsCount = viewsCount,
        authorName = authorName ?: "Équipe Médicale Cléa",
        imageUrl = getEffectiveImageUrl(),
        isFavorite = isFavorite,
        publishedAt = publishedAt
    )
}

fun ArticleEntity.toDomain(): Article {
    return Article(
        id = id,
        title = title,
        slug = slug,
        category = ArticleCategory.fromSlug(category),
        categoryName = categoryName,
        summary = summary,
        content = content,
        readingTimeMinutes = readingTimeMinutes,
        viewsCount = viewsCount,
        authorName = authorName,
        imageUrl = imageUrl,
        isFavorite = isFavorite,
        publishedAt = publishedAt
    )
}

fun ArticleDto.toDomain(): Article {
    val categorySlug = getCategorySlug()
    val categoryLabel = getCategoryName()
    return Article(
        id = id,
        title = title,
        slug = getEffectiveSlug(),
        category = ArticleCategory.fromSlug(categorySlug),
        categoryName = categoryLabel,
        summary = getEffectiveSummary(),
        content = getEffectiveContent(),
        readingTimeMinutes = readingTimeMinutes,
        viewsCount = viewsCount,
        authorName = authorName ?: "Équipe Médicale Cléa",
        imageUrl = getEffectiveImageUrl(),
        isFavorite = isFavorite,
        publishedAt = publishedAt
    )
}
