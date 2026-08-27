package com.drcmind.cleaapp.data.repository

import com.drcmind.cleaapp.data.local.room.dao.MenstrualDao
import com.drcmind.cleaapp.data.mapper.toDomain
import com.drcmind.cleaapp.data.mapper.toEntity
import com.drcmind.cleaapp.data.model.CycleStatus
import com.drcmind.cleaapp.data.remote.api.MenstrualApiService
import com.drcmind.cleaapp.data.remote.dto.*
import com.drcmind.cleaapp.domain.model.*
import com.drcmind.cleaapp.domain.repository.MenstrualRepository

class MenstrualRepositoryImpl(
    private val api: MenstrualApiService,
    private val dao: MenstrualDao
) : MenstrualRepository {

    override suspend fun getCycles(): Result<List<MenstrualCycle>> = try {
        val remote = api.getCycles()
        dao.insertCycles(remote.map { it.toEntity() })
        Result.success(remote.map { it.toDomain() })
    } catch (e: Exception) {
        val local = dao.getAllCycles()
        if (local.isNotEmpty()) Result.success(local.map { it.toDomain() }) else Result.failure(e)
    }

    override suspend fun createCycle(startDate: String, notes: String?): Result<MenstrualCycle> = try {
        val cycle = api.createCycle(startDate, notes)
        dao.insertCycle(cycle.toEntity())
        Result.success(cycle.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getCycle(id: String): Result<MenstrualCycle> = try {
        val dto = api.getCycle(id)
        dao.insertCycle(dto.toEntity())
        dao.insertDays(dto.cycleDays.map { it.toEntity() })
        Result.success(dto.toDomain())
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
        val updated = api.updateCycle(id, params)
        dao.insertCycle(updated.toEntity())
        Result.success(updated.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteCycle(id: String): Result<Unit> = try {
        api.deleteCycle(id)
        dao.deleteCycle(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun completeCycle(id: String, endDate: String, cycleLength: Int, periodLength: Int): Result<MenstrualCycle> = try {
        val completed = api.completeCycle(id, endDate, cycleLength, periodLength)
        dao.insertCycle(completed.toEntity())
        Result.success(completed.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getCycleDays(cycleId: String): Result<List<CycleDay>> = try {
        val remote = api.getCycleDays(cycleId)
        dao.insertDays(remote.map { it.toEntity() })
        Result.success(remote.map { it.toDomain() })
    } catch (e: Exception) {
        Result.success(dao.getDaysForCycle(cycleId).map { it.toDomain() })
    }

    override suspend fun addCycleDay(cycleId: String, date: String, flow: String, painLevel: Int?, mood: String?, temperature: Float?, weight: Float?, medications: String?, notes: String?, symptomIds: List<String>?): Result<CycleDay> = try {
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
                val generatedId = java.util.UUID.randomUUID().toString()
                val localEntity = com.drcmind.cleaapp.data.local.room.entity.DayEntity(
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
                // Continuer avec la persistance locale
            }
            val crossRefs = symptomIds.map { 
                com.drcmind.cleaapp.data.local.room.entity.DaySymptomCrossRef(dayId = dayResult.id, symptomId = it) 
            }
            dao.insertDaySymptomCrossRef(crossRefs)
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
        val updated = api.updateCycleDay(cycleId, dayId, request)
        dao.insertDay(updated.toEntity())
        Result.success(updated.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteCycleDay(cycleId: String, dayId: String): Result<Unit> = try {
        api.deleteCycleDay(cycleId, dayId)
        dao.deleteDay(dayId)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun attachSymptomsToDay(cycleId: String, dayId: String, symptoms: List<Pair<String, Int>>): Result<Unit> = try {
        val payloads = symptoms.map { SymptomItemPayload(id = it.first, severity = it.second) }
        val symptomIds = symptoms.map { it.first }
        api.attachSymptomsToDay(cycleId, dayId, AttachSymptomsRequest(symptoms = payloads, symptomIds = symptomIds))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getSymptoms(): Result<List<Symptom>> = try {
        val remote = api.getSymptoms()
        if (remote.isNotEmpty()) {
            dao.insertSymptoms(remote.map { it.toEntity() })
            Result.success(remote.map { it.toDomain() })
        } else {
            val local = dao.getAllSymptoms()
            if (local.isNotEmpty()) Result.success(local.map { it.toDomain() })
            else Result.success(DEFAULT_SYMPTOMS)
        }
    } catch (e: Exception) {
        val local = dao.getAllSymptoms()
        if (local.isNotEmpty()) Result.success(local.map { it.toDomain() })
        else Result.success(DEFAULT_SYMPTOMS)
    }

    override suspend fun getPredictions(): Result<Prediction> = try {
        Result.success(api.getPredictions().toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getDashboard(): Result<MenstrualDashboard> = try {
        val remote = api.getDashboard()
        remote.activeCycle?.let { dao.insertCycle(it.toEntity()) }
        remote.activeCycle?.cycleDays?.map { it.toEntity() }?.let { dao.insertDays(it) }
        Result.success(remote.toDomain())
    } catch (e: Exception) {
        // Offline fallback: construire dashboard depuis le cache local
        val activeCycleEntity = dao.getActiveCycle(CycleStatus.ACTIVE)
        val activeCycle = activeCycleEntity?.let { cycle ->
            val days = dao.getActiveCycleDays(CycleStatus.ACTIVE)
            cycle.toDomain(days.map { it.toDomain() })
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
        Result.success(MenstrualDashboard(
            activeCycle = activeCycle,
            predictions = null, // Prédictions non disponibles hors-ligne
            stats = stats
        ))
    }
}
