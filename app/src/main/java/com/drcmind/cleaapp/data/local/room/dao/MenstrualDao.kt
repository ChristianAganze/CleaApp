package com.drcmind.cleaapp.data.local.room.dao

import androidx.room.*
import com.drcmind.cleaapp.data.local.room.entity.*
import com.drcmind.cleaapp.data.model.CycleStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MenstrualDao {

    // --- Cycles ---
    @Query("SELECT * FROM cycles ORDER BY startDate DESC")
    fun getCyclesFlow(): Flow<List<CycleEntity>>

    @Query("SELECT * FROM cycles ORDER BY startDate DESC")
    suspend fun getAllCycles(): List<CycleEntity>

    @Query("SELECT * FROM cycles WHERE id = :id")
    suspend fun getCycleById(id: String): CycleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycles(cycles: List<CycleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycle(cycle: CycleEntity)

    @Query("DELETE FROM cycles WHERE id = :id")
    suspend fun deleteCycle(id: String)

    @Query("SELECT * FROM cycles WHERE UPPER(status) = UPPER(:status) ORDER BY startDate DESC LIMIT 1")
    suspend fun getActiveCycle(status: CycleStatus): CycleEntity?

    @Query("SELECT * FROM cycles WHERE UPPER(status) = UPPER(:status) ORDER BY startDate DESC")
    suspend fun getCompletedCycles(status: CycleStatus): List<CycleEntity>

    @Transaction
    @Query("""
        SELECT * FROM cycle_days 
        WHERE cycleId = (SELECT id FROM cycles WHERE UPPER(status) = UPPER(:status) ORDER BY startDate DESC LIMIT 1)
        ORDER BY date ASC
    """)
    suspend fun getActiveCycleDays(status: CycleStatus): List<DayEntity>

    // --- Days ---
    @Query("SELECT * FROM cycle_days WHERE cycleId = :cycleId ORDER BY date ASC")
    suspend fun getDaysForCycle(cycleId: String): List<DayEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDays(days: List<DayEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: DayEntity)

    @Query("DELETE FROM cycle_days WHERE id = :id")
    suspend fun deleteDay(id: String)

    // --- Symptoms ---
    @Query("SELECT * FROM symptoms")
    suspend fun getAllSymptoms(): List<SymptomEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptoms(symptoms: List<SymptomEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDaySymptomCrossRef(crossRef: List<DaySymptomCrossRef>)

    @Query("""
        SELECT * FROM symptoms 
        WHERE id IN (SELECT symptomId FROM day_symptoms WHERE dayId = :dayId)
    """)
    suspend fun getSymptomsForDay(dayId: String): List<SymptomEntity>
}
