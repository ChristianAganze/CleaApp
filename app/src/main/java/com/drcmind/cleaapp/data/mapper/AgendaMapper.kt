package com.drcmind.cleaapp.data.mapper

import com.drcmind.cleaapp.data.local.room.entity.AgendaItemEntity
import com.drcmind.cleaapp.data.remote.dto.AgendaItemDto
import com.drcmind.cleaapp.domain.model.AgendaItem
import com.drcmind.cleaapp.domain.model.AgendaItemCategory
import com.drcmind.cleaapp.domain.model.AgendaItemStatus
import com.drcmind.cleaapp.domain.model.AgendaKind

fun AgendaItemDto.toEntity(): AgendaItemEntity {
    return AgendaItemEntity(
        id = id,
        kind = kind,
        title = title,
        notes = notes,
        category = category,
        dueAt = dueAt,
        remindAt = remindAt,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun AgendaItemEntity.toDomain(): AgendaItem {
    return AgendaItem(
        id = id,
        kind = AgendaKind.fromApi(kind),
        title = title,
        notes = notes,
        category = AgendaItemCategory.fromApi(category),
        dueAt = dueAt,
        remindAt = remindAt,
        status = AgendaItemStatus.fromApi(status),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun AgendaItemDto.toDomain(): AgendaItem {
    return AgendaItem(
        id = id,
        kind = AgendaKind.fromApi(kind),
        title = title,
        notes = notes,
        category = AgendaItemCategory.fromApi(category),
        dueAt = dueAt,
        remindAt = remindAt,
        status = AgendaItemStatus.fromApi(status),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
