package com.drcmind.cleaapp.data.local.room.dao

import androidx.room.*
import com.drcmind.cleaapp.data.local.room.entity.AgendaItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AgendaDao {

    @Query("SELECT * FROM agenda_items ORDER BY CASE WHEN dueAt IS NULL THEN 1 ELSE 0 END, dueAt ASC, createdAt DESC")
    fun getAllItems(): Flow<List<AgendaItemEntity>>

    @Query("SELECT * FROM agenda_items WHERE category = :category ORDER BY CASE WHEN dueAt IS NULL THEN 1 ELSE 0 END, dueAt ASC")
    fun getItemsByCategory(category: String): Flow<List<AgendaItemEntity>>

    @Query("SELECT * FROM agenda_items WHERE id = :id")
    suspend fun getItemById(id: String): AgendaItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: AgendaItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<AgendaItemEntity>)

    @Query("UPDATE agenda_items SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: String)

    @Query("DELETE FROM agenda_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM agenda_items")
    suspend fun clearAll()
}
