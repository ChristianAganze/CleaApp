package com.drcmind.cleaapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AgendaListResponseDto(
    val data: List<AgendaItemDto> = emptyList(),
    val meta: PaginationMetaDto? = null
)

@Serializable
data class PaginationMetaDto(
    @SerialName("current_page") val currentPage: Int = 1,
    @SerialName("per_page") val perPage: Int = 15,
    val total: Int = 0,
    @SerialName("last_page") val lastPage: Int = 1
)

@Serializable
data class AgendaItemDto(
    val id: String,
    val kind: String = "task", // "task" ou "reminder"
    val title: String,
    val notes: String? = null,
    val category: String = "personal", // "work", "family", "spirituality", "personal"
    @SerialName("due_at") val dueAt: String? = null,
    @SerialName("remind_at") val remindAt: String? = null,
    val status: String = "pending", // "pending", "done", "cancelled"
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class CreateAgendaItemRequest(
    val kind: String,
    val title: String,
    val category: String,
    val notes: String? = null,
    @SerialName("due_at") val dueAt: String? = null,
    @SerialName("remind_at") val remindAt: String? = null
)

@Serializable
data class UpdateAgendaItemRequest(
    val kind: String? = null,
    val title: String? = null,
    val category: String? = null,
    val notes: String? = null,
    @SerialName("due_at") val dueAt: String? = null,
    @SerialName("remind_at") val remindAt: String? = null,
    val status: String? = null
)
