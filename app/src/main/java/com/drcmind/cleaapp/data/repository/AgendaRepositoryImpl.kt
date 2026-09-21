package com.drcmind.cleaapp.data.repository

import com.drcmind.cleaapp.data.local.room.dao.AgendaDao
import com.drcmind.cleaapp.data.mapper.toDomain
import com.drcmind.cleaapp.data.mapper.toEntity
import com.drcmind.cleaapp.data.remote.api.AgendaApiService
import com.drcmind.cleaapp.data.remote.dto.CreateAgendaItemRequest
import com.drcmind.cleaapp.domain.model.AgendaItem
import com.drcmind.cleaapp.domain.model.AgendaItemCategory
import com.drcmind.cleaapp.domain.model.AgendaItemStatus
import com.drcmind.cleaapp.domain.model.AgendaKind
import com.drcmind.cleaapp.domain.repository.AgendaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

class AgendaRepositoryImpl(
    private val api: AgendaApiService,
    private val dao: AgendaDao
) : AgendaRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun getLocalAgendaItems(category: AgendaItemCategory?): Flow<List<AgendaItem>> {
        return if (category == null) {
            dao.getAllItems().map { list -> list.map { it.toDomain() } }
        } else {
            dao.getItemsByCategory(category.apiValue).map { list -> list.map { it.toDomain() } }
        }
    }

    override suspend fun refreshAgendaItems(category: AgendaItemCategory?): Result<List<AgendaItem>> = try {
        val dtos = api.getAgendaItems(category = category?.apiValue)
        val entities = dtos.map { it.toEntity() }
        dao.insertAll(entities)
        Result.success(dtos.map { it.toDomain() })
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun createItem(
        kind: AgendaKind,
        title: String,
        category: AgendaItemCategory,
        notes: String?,
        dueAt: String?,
        remindAt: String?
    ): Result<AgendaItem> = try {
        val request = CreateAgendaItemRequest(
            kind = kind.apiValue,
            title = title,
            category = category.apiValue,
            notes = notes,
            dueAt = dueAt,
            remindAt = remindAt
        )
        val responseDto = try {
            api.createAgendaItem(request)
        } catch (e: Exception) {
            // Fallback en création locale offline
            val localId = "loc_${UUID.randomUUID()}"
            com.drcmind.cleaapp.data.remote.dto.AgendaItemDto(
                id = localId,
                kind = kind.apiValue,
                title = title,
                category = category.apiValue,
                notes = notes,
                dueAt = dueAt,
                remindAt = remindAt,
                status = "pending",
                createdAt = dateFormat.format(Date()),
                updatedAt = dateFormat.format(Date())
            )
        }
        dao.insertOrUpdate(responseDto.toEntity())
        Result.success(responseDto.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun toggleItemStatus(id: String, currentlyDone: Boolean): Result<Unit> = try {
        val newStatus = if (currentlyDone) AgendaItemStatus.PENDING else AgendaItemStatus.DONE
        dao.updateStatus(id, newStatus.apiValue, dateFormat.format(Date()))
        try {
            if (currentlyDone) {
                api.reopenItem(id)
            } else {
                api.completeItem(id)
            }
        } catch (_: Exception) {}
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteItem(id: String): Result<Unit> = try {
        dao.deleteById(id)
        try {
            api.deleteAgendaItem(id)
        } catch (_: Exception) {}
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getItemDetail(id: String): Result<AgendaItem> = try {
        val local = dao.getItemById(id)?.toDomain()
        if (local != null) {
            Result.success(local)
        } else {
            val remote = api.getAgendaItemDetail(id).toDomain()
            Result.success(remote)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
