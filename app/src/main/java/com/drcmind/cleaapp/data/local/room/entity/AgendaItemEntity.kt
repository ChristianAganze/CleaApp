package com.drcmind.cleaapp.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agenda_items")
data class AgendaItemEntity(
    @PrimaryKey
    val id: String,
    val kind: String,
    val title: String,
    val notes: String?,
    val category: String,
    val dueAt: String?,
    val remindAt: String?,
    val status: String,
    val createdAt: String?,
    val updatedAt: String?
)
