package com.drcmind.cleaapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drcmind.cleaapp.data.local.datastore.AuthDataStore
import com.drcmind.cleaapp.domain.model.HomeDashboard
import com.drcmind.cleaapp.domain.model.User
import com.drcmind.cleaapp.domain.repository.AuthRepository
import com.drcmind.cleaapp.domain.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val dashboard: HomeDashboard? = null,
    val user: User? = null,
    val error: String? = null
)

class HomeViewModel(
    private val homeRepository: HomeRepository,
    private val authRepository: AuthRepository,
    private val authDataStore: AuthDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = !isRefresh && it.dashboard == null, isRefreshing = isRefresh) }
            
            val userResult = authRepository.getUser()
            val user = when (userResult) {
                is com.drcmind.cleaapp.domain.model.AuthResult.Success -> userResult.data
                else -> {
                    val cachedName = authDataStore.userName.firstOrNull()
                    val cachedEmail = authDataStore.userEmail.firstOrNull()
                    if (!cachedName.isNullOrBlank()) {
                        User(id = "local", name = cachedName, email = cachedEmail ?: "", role = "user")
                    } else null
                }
            }
            val dashboardResult = homeRepository.getHomeDashboard()

            _state.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    isRefreshing = false,
                    dashboard = dashboardResult.getOrNull() ?: currentState.dashboard,
                    user = user,
                    error = if (dashboardResult.isFailure && currentState.dashboard == null) {
                        dashboardResult.exceptionOrNull()?.message
                    } else null
                )
            }
        }
    }

    fun toggleTask(taskId: String, currentlyDone: Boolean) {
        viewModelScope.launch {
            // Mise à jour optimiste dans la liste des tâches de l'UI
            val currentDashboard = _state.value.dashboard
            if (currentDashboard != null) {
                val updatedItems = currentDashboard.todayTasks.items.map { item ->
                    if (item.id == taskId) item.copy(isDone = !currentlyDone) else item
                }
                val newDoneCount = updatedItems.count { it.isDone }
                val updatedSummary = currentDashboard.todayTasks.copy(
                    items = updatedItems,
                    done = newDoneCount
                )
                _state.update { it.copy(dashboard = currentDashboard.copy(todayTasks = updatedSummary)) }
            }

            homeRepository.toggleTaskStatus(taskId, currentlyDone)
        }
    }
}
