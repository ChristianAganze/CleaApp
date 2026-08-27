package com.drcmind.cleaapp.data.mapper

import com.drcmind.cleaapp.data.local.room.entity.CycleEntity
import com.drcmind.cleaapp.data.local.room.entity.DayEntity
import com.drcmind.cleaapp.data.local.room.entity.SymptomEntity
import com.drcmind.cleaapp.data.remote.dto.*
import com.drcmind.cleaapp.domain.model.*

// --- DTO to Domain ---

fun CycleDto.toDomain(): MenstrualCycle {
    return MenstrualCycle(
        id = id,
        startDate = startDate,
        endDate = endDate,
        cycleLength = cycleLength,
        periodLength = periodLength,
        status = status.name,
        notes = notes,
        cycleDays = cycleDays.map { it.toDomain() }
    )
}

fun CycleDayDto.toDomain(): CycleDay {
    return CycleDay(
        id = id,
        date = date,
        flow = flow.name,
        painLevel = painLevel,
        mood = mood?.name,
        temperature = temperature,
        weight = weight,
        medications = medications,
        notes = notes,
        symptoms = symptoms.map { it.toDomain() }
    )
}

fun SymptomDto.toDomain(): Symptom {
    return Symptom(
        id = id,
        name = name,
        slug = slug,
        description = description,
        icon = icon
    )
}

fun PredictionDto.toDomain(): Prediction {
    return Prediction(
        averageCycleLength = averageCycleLength,
        averagePeriodLength = averagePeriodLength,
        cycleVariation = cycleVariation,
        predictedPeriodStart = predictedPeriodStart,
        predictedPeriodEnd = predictedPeriodEnd,
        predictedOvulation = predictedOvulation,
        fertilityStart = fertilityStart,
        fertilityEnd = fertilityEnd,
        confidence = confidence ?: 0f
    )
}

fun StatisticsDto.toDomain(): MenstrualStats {
    return MenstrualStats(
        completedCyclesCount = completedCyclesCount,
        averageCycleLength = averageCycleLength,
        averagePeriodLength = averagePeriodLength,
        cycleVariation = cycleVariation
    )
}

fun DashboardDto.toDomain(): MenstrualDashboard {
    return MenstrualDashboard(
        activeCycle = activeCycle?.toDomain(),
        predictions = predictions?.toDomain(),
        stats = statistics.toDomain()
    )
}

// --- DTO to Entity ---

fun CycleDto.toEntity(): CycleEntity {
    return CycleEntity(
        id = id,
        startDate = startDate,
        endDate = endDate,
        cycleLength = cycleLength,
        periodLength = periodLength,
        status = status.name,
        notes = notes
    )
}

fun CycleDayDto.toEntity(): DayEntity {
    return DayEntity(
        id = id,
        cycleId = cycleId,
        date = date,
        flow = flow.name,
        painLevel = painLevel,
        mood = mood?.name,
        temperature = temperature,
        weight = weight,
        medications = medications,
        notes = notes
    )
}

fun SymptomDto.toEntity(): SymptomEntity {
    return SymptomEntity(
        id = id,
        name = name,
        slug = slug,
        description = description,
        icon = icon
    )
}

// --- Entity to Domain ---

fun CycleEntity.toDomain(days: List<CycleDay> = emptyList()): MenstrualCycle {
    return MenstrualCycle(
        id = id,
        startDate = startDate,
        endDate = endDate,
        cycleLength = cycleLength,
        periodLength = periodLength,
        status = status,
        notes = notes,
        cycleDays = days
    )
}

fun DayEntity.toDomain(symptoms: List<Symptom> = emptyList()): CycleDay {
    return CycleDay(
        id = id,
        date = date,
        flow = flow,
        painLevel = painLevel,
        mood = mood,
        temperature = temperature,
        weight = weight,
        medications = medications,
        notes = notes,
        symptoms = symptoms
    )
}

fun SymptomEntity.toDomain(): Symptom {
    return Symptom(
        id = id,
        name = name,
        slug = slug,
        description = description,
        icon = icon
    )
}
