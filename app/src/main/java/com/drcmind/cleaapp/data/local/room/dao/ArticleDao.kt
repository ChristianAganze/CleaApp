package com.drcmind.cleaapp.data.local.room.dao

import androidx.room.*
import com.drcmind.cleaapp.data.local.room.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    @Query("SELECT * FROM articles ORDER BY publishedAt DESC")
    fun getAllArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE category = :category ORDER BY publishedAt DESC")
    fun getArticlesByCategory(category: String): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE isFavorite = 1 ORDER BY publishedAt DESC")
    fun getFavoriteArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE id = :id")
    suspend fun getArticleById(id: String): ArticleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<ArticleEntity>)

    @Query("UPDATE articles SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("DELETE FROM articles")
    suspend fun clearAll()
}
