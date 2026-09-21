package com.drcmind.cleaapp.domain.repository

import com.drcmind.cleaapp.domain.model.Article
import com.drcmind.cleaapp.domain.model.ArticleCategory
import kotlinx.coroutines.flow.Flow

interface ArticleRepository {
    fun getLocalArticles(category: ArticleCategory? = null, favoritesOnly: Boolean = false): Flow<List<Article>>
    suspend fun refreshArticles(category: ArticleCategory? = null, search: String? = null): Result<List<Article>>
    suspend fun toggleFavorite(articleId: String, currentFavorite: Boolean): Result<Unit>
    suspend fun getArticleDetail(articleId: String): Result<Article>
}
