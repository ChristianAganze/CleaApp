package com.drcmind.cleaapp.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val category: String,
    val summary: String,
    val content: String,
    val readingTimeMinutes: Int,
    val authorName: String,
    val imageUrl: String?,
    val isFavorite: Boolean,
    val publishedAt: String?
)
