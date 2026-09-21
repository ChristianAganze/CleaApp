package com.drcmind.cleaapp.domain.repository

import com.drcmind.cleaapp.domain.model.AgendaItem
import com.drcmind.cleaapp.domain.model.AgendaItemCategory
import com.drcmind.cleaapp.domain.model.AgendaKind
import kotlinx.coroutines.flow.Flow

interface AgendaRepository {
    fun getLocalAgendaItems(category: AgendaItemCategory? = null): Flow<List<AgendaItem>>
    suspend fun refreshAgendaItems(category: AgendaItemCategory? = null): Result<List<AgendaItem>>
    suspend fun createItem(
        kind: AgendaKind,
        title: String,
        category: AgendaItemCategory,
        notes: String?,
        dueAt: String?,
        remindAt: String?
    ): Result<AgendaItem>
    suspend fun toggleItemStatus(id: String, currentlyDone: Boolean): Result<Unit>
    suspend fun deleteItem(id: String): Result<Unit>
    suspend fun getItemDetail(id: String): Result<AgendaItem>
}
