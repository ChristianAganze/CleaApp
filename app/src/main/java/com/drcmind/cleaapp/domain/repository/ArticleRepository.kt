package com.drcmind.cleaapp.domain.repository

import com.drcmind.cleaapp.data.remote.dto.CategoryDto
import com.drcmind.cleaapp.domain.model.Article
import com.drcmind.cleaapp.domain.model.ArticleCategory
import kotlinx.coroutines.flow.Flow

interface ArticleRepository {
    fun getLocalArticles(category: ArticleCategory? = null, favoritesOnly: Boolean = false): Flow<List<Article>>
    suspend fun refreshArticles(category: ArticleCategory? = null, search: String? = null, favoritesOnly: Boolean = false): Result<List<Article>>
    suspend fun getCategories(): Result<List<CategoryDto>>
    suspend fun toggleFavorite(slugOrId: String, currentFavorite: Boolean): Result<Unit>
    suspend fun getArticleDetail(slugOrId: String): Result<Article>
}
