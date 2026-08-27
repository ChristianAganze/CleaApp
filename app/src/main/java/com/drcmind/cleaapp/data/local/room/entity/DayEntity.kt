package com.drcmind.cleaapp.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cycle_days",
    indices = [Index(value = ["cycleId"])]
)
data class DayEntity(
    @PrimaryKey val id: String,
    val cycleId: String,
    val date: String,
    val flow: String,
    val painLevel: Int?,
    val mood: String?,
    val temperature: Float?,
    val weight: Float?,
    val medications: String?,
    val notes: String?
)
