package com.drcmind.cleaapp.data.mapper

import com.drcmind.cleaapp.data.local.room.entity.ArticleEntity
import com.drcmind.cleaapp.data.remote.dto.ArticleDto
import com.drcmind.cleaapp.domain.model.Article
import com.drcmind.cleaapp.domain.model.ArticleCategory

fun ArticleDto.toEntity(): ArticleEntity {
    return ArticleEntity(
        id = id,
        title = title,
        category = category,
        summary = summary,
        content = content,
        readingTimeMinutes = readingTimeMinutes,
        authorName = authorName ?: "Équipe Cléa",
        imageUrl = imageUrl,
        isFavorite = isFavorite,
        publishedAt = publishedAt
    )
}

fun ArticleEntity.toDomain(): Article {
    return Article(
        id = id,
        title = title,
        category = ArticleCategory.fromApi(category),
        summary = summary,
        content = content,
        readingTimeMinutes = readingTimeMinutes,
        authorName = authorName,
        imageUrl = imageUrl,
        isFavorite = isFavorite,
        publishedAt = publishedAt
    )
}

fun ArticleDto.toDomain(): Article {
    return Article(
        id = id,
        title = title,
        category = ArticleCategory.fromApi(category),
        summary = summary,
        content = content,
        readingTimeMinutes = readingTimeMinutes,
        authorName = authorName ?: "Équipe Cléa",
        imageUrl = imageUrl,
        isFavorite = isFavorite,
        publishedAt = publishedAt
    )
}
