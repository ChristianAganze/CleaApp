package com.drcmind.cleaapp.ui.menstrual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drcmind.cleaapp.domain.model.AuthResult
import com.drcmind.cleaapp.domain.model.MenstrualDashboard
import com.drcmind.cleaapp.domain.model.Symptom
import com.drcmind.cleaapp.domain.model.User
import com.drcmind.cleaapp.domain.repository.AuthRepository
import com.drcmind.cleaapp.domain.repository.MenstrualRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class MenstrualState(
    val dashboard: MenstrualDashboard? = null,
    val symptoms: List<Symptom> = emptyList(),
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class MenstrualViewModel(
    private val repository: MenstrualRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MenstrualState())
    val state = _state.asStateFlow()

    init {
        refreshAll()
    }

    private fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun refreshAll() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val symptomsDeferred = async { repository.getSymptoms() }
            val dashboardDeferred = async { repository.getDashboard() }
            val userDeferred = async { authRepository.getUser() }

            val symptomsResult = symptomsDeferred.await()
            val dashboardResult = dashboardDeferred.await()
            val userResult = userDeferred.await()

            _state.update { currentState ->
                val user = when (userResult) {
                    is AuthResult.Success -> userResult.data
                    else -> currentState.user
                }
                currentState.copy(
                    isLoading = false,
                    symptoms = symptomsResult.getOrDefault(currentState.symptoms),
                    // On ne remplace par null que si on n'avait rien avant
                    dashboard = dashboardResult.getOrNull() ?: currentState.dashboard,
                    user = user,
                    error = if (dashboardResult.isFailure) dashboardResult.exceptionOrNull()?.message else null
                )
            }
        }
    }

    fun startNewCycle(startDate: String? = null, notes: String? = null) {
        val date = startDate ?: getCurrentDateString()
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.createCycle(date, notes)
                .onSuccess {
                    refreshAll()
                }
                .onFailure { error ->
                    _state.update { it.copy(error = error.message, isLoading = false) }
                }
        }
    }

    fun completeCycle(id: String, endDate: String, cycleLength: Int, periodLength: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.completeCycle(id, endDate, cycleLength, periodLength)
                .onSuccess {
                    refreshAll()
                }
                .onFailure { error ->
                    _state.update { it.copy(error = error.message, isLoading = false) }
                }
        }
    }

    fun addDailyLog(cycleId: String, date: String, flow: String, painLevel: Int, mood: String, symptomIds: List<String>) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isSuccess = false, error = null) }
            var targetCycleId = cycleId
            if (targetCycleId.isBlank()) {
                val active = _state.value.dashboard?.activeCycle
                if (active != null) {
                    targetCycleId = active.id
                } else {
                    val createResult = repository.createCycle(date, null)
                    if (createResult.isSuccess) {
                        targetCycleId = createResult.getOrNull()?.id ?: ""
                    }
                }
            }
            if (targetCycleId.isBlank()) {
                _state.update { it.copy(error = "Veuillez d'abord démarrer un cycle actif.", isLoading = false) }
                return@launch
            }
            repository.addCycleDay(
                cycleId = targetCycleId, date = date, flow = flow, painLevel = painLevel,
                mood = mood, temperature = null, weight = null, medications = null, notes = null,
                symptomIds = symptomIds
            ).onSuccess {
                _state.update { it.copy(isSuccess = true) }
                refreshAll()
            }.onFailure { error ->
                _state.update { it.copy(error = error.message ?: "Erreur lors de l'enregistrement", isLoading = false) }
            }
        }
    }
    
    fun resetSuccess() {
        _state.update { it.copy(isSuccess = false) }
    }
}
