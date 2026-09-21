package com.drcmind.cleaapp.ui.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drcmind.cleaapp.domain.model.AgendaItem
import com.drcmind.cleaapp.domain.model.AgendaItemCategory
import com.drcmind.cleaapp.domain.model.AgendaKind
import com.drcmind.cleaapp.domain.repository.AgendaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AgendaUiState(
    val isLoading: Boolean = false,
    val items: List<AgendaItem> = emptyList(),
    val selectedCategory: AgendaItemCategory? = null,
    val selectedStatusFilter: StatusFilter = StatusFilter.ALL,
    val error: String? = null,
    val isCreating: Boolean = false
)

enum class StatusFilter(val label: String) {
    ALL("Toutes"),
    PENDING("À faire"),
    DONE("Terminées")
}

@OptIn(ExperimentalCoroutinesApi::class)
class AgendaViewModel(
    private val agendaRepository: AgendaRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<AgendaItemCategory?>(null)
    private val _selectedStatusFilter = MutableStateFlow(StatusFilter.ALL)
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _isCreating = MutableStateFlow(false)

    private val _itemsFlow = _selectedCategory.flatMapLatest { cat ->
        agendaRepository.getLocalAgendaItems(cat)
    }

    val state: StateFlow<AgendaUiState> = combine(
        combine(_itemsFlow, _selectedCategory, _selectedStatusFilter) { items, category, statusFilter ->
            Triple(items, category, statusFilter)
        },
        combine(_isLoading, _error, _isCreating) { loading, error, creating ->
            Triple(loading, error, creating)
        }
    ) { (items, category, statusFilter), (loading, error, creating) ->
        val filtered = when (statusFilter) {
            StatusFilter.ALL -> items
            StatusFilter.PENDING -> items.filter { !it.isDone }
            StatusFilter.DONE -> items.filter { it.isDone }
        }
        AgendaUiState(
            isLoading = loading,
            items = filtered,
            selectedCategory = category,
            selectedStatusFilter = statusFilter,
            error = error,
            isCreating = creating
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AgendaUiState(isLoading = true))

    init {
        refresh()
    }

    fun setCategory(category: AgendaItemCategory?) {
        _selectedCategory.value = category
    }

    fun setStatusFilter(filter: StatusFilter) {
        _selectedStatusFilter.value = filter
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = agendaRepository.refreshAgendaItems(_selectedCategory.value)
            _isLoading.value = false
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun toggleTask(item: AgendaItem) {
        viewModelScope.launch {
            agendaRepository.toggleItemStatus(item.id, item.isDone)
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            agendaRepository.deleteItem(id)
        }
    }

    fun createTask(
        kind: AgendaKind,
        title: String,
        category: AgendaItemCategory,
        notes: String?,
        dueAt: String?,
        remindAt: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isCreating.value = true
            val result = agendaRepository.createItem(
                kind = kind,
                title = title,
                category = category,
                notes = notes,
                dueAt = dueAt,
                remindAt = remindAt
            )
            _isCreating.value = false
            if (result.isSuccess) {
                onSuccess()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Erreur lors de la création"
            }
        }
    }
}
