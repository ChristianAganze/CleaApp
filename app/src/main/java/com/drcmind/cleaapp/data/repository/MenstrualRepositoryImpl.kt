package com.drcmind.cleaapp.data.repository

import com.drcmind.cleaapp.data.local.room.dao.MenstrualDao
import com.drcmind.cleaapp.data.local.room.entity.CycleEntity
import com.drcmind.cleaapp.data.local.room.entity.DayEntity
import com.drcmind.cleaapp.data.local.room.entity.DaySymptomCrossRef
import com.drcmind.cleaapp.data.mapper.toDomain
import com.drcmind.cleaapp.data.mapper.toEntity
import com.drcmind.cleaapp.data.model.CycleStatus
import com.drcmind.cleaapp.data.remote.api.MenstrualApiService
import com.drcmind.cleaapp.data.remote.dto.*
import com.drcmind.cleaapp.domain.model.*
import com.drcmind.cleaapp.domain.repository.MenstrualRepository
import java.util.UUID

class MenstrualRepositoryImpl(
    private val api: MenstrualApiService,
    private val dao: MenstrualDao
) : MenstrualRepository {

    override suspend fun getCycles(): Result<List<MenstrualCycle>> = try {
        val remote = try {
            api.getCycles()
        } catch (e: Exception) {
            emptyList()
        }
        if (remote.isNotEmpty()) {
            dao.insertCycles(remote.map { it.toEntity() })
            Result.success(remote.map { it.toDomain() })
        } else {
            val local = dao.getAllCycles()
            Result.success(local.map { it.toDomain() })
        }
    } catch (e: Exception) {
        val local = dao.getAllCycles()
        Result.success(local.map { it.toDomain() })
    }

    override suspend fun createCycle(startDate: String, notes: String?): Result<MenstrualCycle> = try {
        val remote = try {
            api.createCycle(startDate, notes)
        } catch (e: Exception) {
            null
        }
        if (remote != null) {
            dao.insertCycle(remote.toEntity())
            Result.success(remote.toDomain())
        } else {
            val generatedId = UUID.randomUUID().toString()
            val localCycle = CycleEntity(
                id = generatedId,
                startDate = startDate,
                endDate = null,
                cycleLength = null,
                periodLength = null,
                status = CycleStatus.ACTIVE.name,
                notes = notes
            )
            dao.insertCycle(localCycle)
            Result.success(localCycle.toDomain())
        }
    } catch (e: Exception) {
        // En cas d'erreur inattendue, assurer au moins la création locale
        val generatedId = UUID.randomUUID().toString()
        val localCycle = CycleEntity(
            id = generatedId,
            startDate = startDate,
            endDate = null,
            cycleLength = null,
            periodLength = null,
            status = CycleStatus.ACTIVE.name,
            notes = notes
        )
        dao.insertCycle(localCycle)
        Result.success(localCycle.toDomain())
    }

    override suspend fun getCycle(id: String): Result<MenstrualCycle> = try {
        val dto = try {
            api.getCycle(id)
        } catch (e: Exception) {
            null
        }
        if (dto != null) {
            dao.insertCycle(dto.toEntity())
            dao.insertDays(dto.cycleDays.map { it.toEntity() })
            Result.success(dto.toDomain())
        } else {
            dao.getCycleById(id)?.let { local ->
                val days = dao.getDaysForCycle(id).map { it.toDomain() }
                Result.success(local.toDomain(days))
            } ?: Result.failure(Exception("Cycle introuvable"))
        }
    } catch (e: Exception) {
        dao.getCycleById(id)?.let { local ->
            val days = dao.getDaysForCycle(id).map { it.toDomain() }
            Result.success(local.toDomain(days))
        } ?: Result.failure(e)
    }

    override suspend fun updateCycle(id: String, startDate: String?, endDate: String?, cycleLength: Int?, periodLength: Int?, status: String?, notes: String?): Result<MenstrualCycle> = try {
        val params = mutableMapOf<String, String>().apply {
            startDate?.let { put("start_date", it) }
            endDate?.let { put("end_date", it) }
            cycleLength?.let { put("cycle_length", it.toString()) }
            periodLength?.let { put("period_length", it.toString()) }
            status?.let { put("status", it) }
            notes?.let { put("notes", it) }
        }
        val updatedDto = try {
            api.updateCycle(id, params)
        } catch (e: Exception) {
            null
        }
        if (updatedDto != null) {
            dao.insertCycle(updatedDto.toEntity())
            Result.success(updatedDto.toDomain())
        } else {
            val existing = dao.getCycleById(id)
            val updated = existing?.copy(
                startDate = startDate ?: existing.startDate,
                endDate = endDate ?: existing.endDate,
                cycleLength = cycleLength ?: existing.cycleLength,
                periodLength = periodLength ?: existing.periodLength,
                status = status ?: existing.status,
                notes = notes ?: existing.notes
            ) ?: CycleEntity(
                id = id,
                startDate = startDate ?: "",
                endDate = endDate,
                cycleLength = cycleLength,
                periodLength = periodLength,
                status = status ?: CycleStatus.ACTIVE.name,
                notes = notes
            )
            dao.insertCycle(updated)
            Result.success(updated.toDomain())
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteCycle(id: String): Result<Unit> = try {
        try { api.deleteCycle(id) } catch (_: Exception) {}
        dao.deleteCycle(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun completeCycle(id: String, endDate: String, cycleLength: Int, periodLength: Int): Result<MenstrualCycle> = try {
        val completed = try {
            api.completeCycle(id, endDate, cycleLength, periodLength)
        } catch (e: Exception) {
            null
        }
        if (completed != null) {
            dao.insertCycle(completed.toEntity())
            Result.success(completed.toDomain())
        } else {
            val existing = dao.getCycleById(id)
            val updated = existing?.copy(
                endDate = endDate,
                cycleLength = cycleLength,
                periodLength = periodLength,
                status = CycleStatus.COMPLETED.name
            ) ?: CycleEntity(
                id = id,
                startDate = endDate,
                endDate = endDate,
                cycleLength = cycleLength,
                periodLength = periodLength,
                status = CycleStatus.COMPLETED.name,
                notes = null
            )
            dao.insertCycle(updated)
            Result.success(updated.toDomain())
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getCycleDays(cycleId: String): Result<List<CycleDay>> = try {
        val remote = try {
            api.getCycleDays(cycleId)
        } catch (e: Exception) {
            emptyList()
        }
        if (remote.isNotEmpty()) {
            dao.insertDays(remote.map { it.toEntity() })
            Result.success(remote.map { it.toDomain() })
        } else {
            val localDays = dao.getDaysForCycle(cycleId)
            val domainDays = localDays.map { dayEntity ->
                val symptoms = dao.getSymptomsForDay(dayEntity.id).map { it.toDomain() }
                dayEntity.toDomain(symptoms)
            }
            Result.success(domainDays)
        }
    } catch (e: Exception) {
        val localDays = dao.getDaysForCycle(cycleId)
        val domainDays = localDays.map { dayEntity ->
            val symptoms = dao.getSymptomsForDay(dayEntity.id).map { it.toDomain() }
            dayEntity.toDomain(symptoms)
        }
        Result.success(domainDays)
    }

    override suspend fun addCycleDay(
        cycleId: String,
        date: String,
        flow: String,
        painLevel: Int?,
        mood: String?,
        temperature: Float?,
        weight: Float?,
        medications: String?,
        notes: String?,
        symptomIds: List<String>?
    ): Result<CycleDay> = try {
        // 1. S'assurer que le cycle existe en local dans la base Room
        val existingCycle = dao.getCycleById(cycleId)
        if (existingCycle == null) {
            dao.insertCycle(
                CycleEntity(
                    id = cycleId,
                    startDate = date,
                    endDate = null,
                    cycleLength = null,
                    periodLength = null,
                    status = CycleStatus.ACTIVE.name,
                    notes = null
                )
            )
        }

        // 2. S'assurer que les symptômes par défaut sont en base pour les correspondances
        val localSymptoms = dao.getAllSymptoms()
        if (localSymptoms.isEmpty()) {
            dao.insertSymptoms(DEFAULT_SYMPTOMS.map { it.toEntity() })
        }

        val existingDays = dao.getDaysForCycle(cycleId)
        val existingDay = existingDays.firstOrNull { it.date == date }

        val dayResult: CycleDay = if (existingDay != null) {
            val updateReq = UpdateCycleDayRequest(
                flow = flow,
                painLevel = painLevel,
                mood = mood,
                temperature = temperature,
                weight = weight,
                medications = medications,
                notes = notes
            )
            val updatedDto = try {
                api.updateCycleDay(cycleId, existingDay.id, updateReq)
            } catch (e: Exception) {
                null
            }
            if (updatedDto != null) {
                dao.insertDay(updatedDto.toEntity())
                updatedDto.toDomain()
            } else {
                val updatedLocal = existingDay.copy(
                    flow = flow,
                    painLevel = painLevel,
                    mood = mood,
                    temperature = temperature,
                    weight = weight,
                    medications = medications,
                    notes = notes
                )
                dao.insertDay(updatedLocal)
                updatedLocal.toDomain()
            }
        } else {
            val createReq = CreateCycleDayRequest(
                date = date,
                flow = flow,
                painLevel = painLevel,
                mood = mood,
                temperature = temperature,
                weight = weight,
                medications = medications,
                notes = notes,
                symptomIds = symptomIds
            )
            val createdDto = try {
                api.addCycleDay(cycleId, createReq)
            } catch (e: Exception) {
                null
            }
            if (createdDto != null) {
                dao.insertDay(createdDto.toEntity())
                createdDto.toDomain()
            } else {
                val generatedId = UUID.randomUUID().toString()
                val localEntity = DayEntity(
                    id = generatedId,
                    cycleId = cycleId,
                    date = date,
                    flow = flow,
                    painLevel = painLevel,
                    mood = mood,
                    temperature = temperature,
                    weight = weight,
                    medications = medications,
                    notes = notes
                )
                dao.insertDay(localEntity)
                localEntity.toDomain()
            }
        }

        if (!symptomIds.isNullOrEmpty()) {
            val symptomsPayload = symptomIds.map { id ->
                SymptomItemPayload(id = id, severity = painLevel?.coerceAtLeast(1) ?: 1)
            }
            try {
                api.attachSymptomsToDay(
                    cycleId = cycleId,
                    dayId = dayResult.id,
                    request = AttachSymptomsRequest(symptoms = symptomsPayload, symptomIds = symptomIds)
                )
            } catch (e: Exception) {
                // Erreur réseau ignorée pour ne pas bloquer l'action locale
            }
            try {
                val crossRefs = symptomIds.map { 
                    DaySymptomCrossRef(dayId = dayResult.id, symptomId = it) 
                }
                dao.insertDaySymptomCrossRef(crossRefs)
            } catch (e: Exception) {
                // Liaison locale protégée
            }
        }

        Result.success(dayResult)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateCycleDay(cycleId: String, dayId: String, flow: String?, painLevel: Int?, mood: String?, temperature: Float?, weight: Float?, medications: String?, notes: String?): Result<CycleDay> = try {
        val request = UpdateCycleDayRequest(
            flow = flow,
            painLevel = painLevel,
            mood = mood,
            temperature = temperature,
            weight = weight,
            medications = medications,
            notes = notes
        )
        val updated = try {
            api.updateCycleDay(cycleId, dayId, request)
        } catch (e: Exception) {
            null
        }
        if (updated != null) {
            dao.insertDay(updated.toEntity())
            Result.success(updated.toDomain())
        } else {
            val existing = dao.getDaysForCycle(cycleId).firstOrNull { it.id == dayId }
            if (existing != null) {
                val updatedLocal = existing.copy(
                    flow = flow ?: existing.flow,
                    painLevel = painLevel ?: existing.painLevel,
                    mood = mood ?: existing.mood,
                    temperature = temperature ?: existing.temperature,
                    weight = weight ?: existing.weight,
                    medications = medications ?: existing.medications,
                    notes = notes ?: existing.notes
                )
                dao.insertDay(updatedLocal)
                Result.success(updatedLocal.toDomain())
            } else {
                Result.failure(Exception("Jour introuvable"))
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteCycleDay(cycleId: String, dayId: String): Result<Unit> = try {
        try { api.deleteCycleDay(cycleId, dayId) } catch (_: Exception) {}
        dao.deleteDay(dayId)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun attachSymptomsToDay(cycleId: String, dayId: String, symptoms: List<Pair<String, Int>>): Result<Unit> = try {
        val payloads = symptoms.map { SymptomItemPayload(id = it.first, severity = it.second) }
        val symptomIds = symptoms.map { it.first }
        try {
            api.attachSymptomsToDay(cycleId, dayId, AttachSymptomsRequest(symptoms = payloads, symptomIds = symptomIds))
        } catch (_: Exception) {}
        val crossRefs = symptomIds.map { DaySymptomCrossRef(dayId = dayId, symptomId = it) }
        dao.insertDaySymptomCrossRef(crossRefs)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getSymptoms(): Result<List<Symptom>> = try {
        val remote = try {
            api.getSymptoms()
        } catch (e: Exception) {
            emptyList()
        }
        if (remote.isNotEmpty()) {
            dao.insertSymptoms(remote.map { it.toEntity() })
            Result.success(remote.map { it.toDomain() })
        } else {
            val local = dao.getAllSymptoms()
            if (local.isNotEmpty()) {
                Result.success(local.map { it.toDomain() })
            } else {
                dao.insertSymptoms(DEFAULT_SYMPTOMS.map { it.toEntity() })
                Result.success(DEFAULT_SYMPTOMS)
            }
        }
    } catch (e: Exception) {
        val local = dao.getAllSymptoms()
        if (local.isNotEmpty()) {
            Result.success(local.map { it.toDomain() })
        } else {
            dao.insertSymptoms(DEFAULT_SYMPTOMS.map { it.toEntity() })
            Result.success(DEFAULT_SYMPTOMS)
        }
    }

    override suspend fun getPredictions(): Result<Prediction> = try {
        Result.success(api.getPredictions().toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getDashboard(): Result<MenstrualDashboard> = try {
        val remote = try {
            api.getDashboard()
        } catch (e: Exception) {
            null
        }
        if (remote != null) {
            remote.activeCycle?.let { dao.insertCycle(it.toEntity()) }
            remote.activeCycle?.cycleDays?.map { it.toEntity() }?.let { dao.insertDays(it) }
            Result.success(remote.toDomain())
        } else {
            buildLocalDashboard()
        }
    } catch (e: Exception) {
        buildLocalDashboard()
    }

    private suspend fun buildLocalDashboard(): Result<MenstrualDashboard> {
        val activeCycleEntity = dao.getActiveCycle(CycleStatus.ACTIVE)
        val activeCycle = activeCycleEntity?.let { cycle ->
            val days = dao.getActiveCycleDays(CycleStatus.ACTIVE)
            val daysWithSymptoms = days.map { dayEntity ->
                val symptoms = dao.getSymptomsForDay(dayEntity.id).map { it.toDomain() }
                dayEntity.toDomain(symptoms)
            }
            cycle.toDomain(daysWithSymptoms)
        }
        val completedCycles = dao.getCompletedCycles(CycleStatus.COMPLETED)
        val stats = if (completedCycles.isNotEmpty()) {
            val lengths = completedCycles.mapNotNull { it.cycleLength }.filter { it > 0 }
            val periodLengths = completedCycles.mapNotNull { it.periodLength }.filter { it > 0 }
            MenstrualStats(
                completedCyclesCount = completedCycles.size,
                averageCycleLength = if (lengths.isNotEmpty()) lengths.average().toInt() else 28,
                averagePeriodLength = if (periodLengths.isNotEmpty()) periodLengths.average().toInt() else 5,
                cycleVariation = if (lengths.size > 1) (lengths.maxOrNull()!! - lengths.minOrNull()!!) else 0
            )
        } else {
            MenstrualStats(0, 28, 5, 0)
        }
        return Result.success(
            MenstrualDashboard(
                activeCycle = activeCycle,
                predictions = null,
                stats = stats
            )
        )
    }
}
