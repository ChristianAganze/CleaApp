package com.drcmind.cleaapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drcmind.cleaapp.domain.model.AuthResult
import com.drcmind.cleaapp.domain.model.User
import com.drcmind.cleaapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isUpdatingProfile: Boolean = false,
    val isUpdatingPassword: Boolean = false,
    val profileSuccessMessage: String? = null,
    val passwordSuccessMessage: String? = null,
    val error: String? = null,
    val fieldErrors: Map<String, List<String>> = emptyMap(),
    val isLoggedOut: Boolean = false
)

class ProfileViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getUser()) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(user = result.data, isLoading = false) }
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun updateProfile(name: String, email: String) {
        if (name.isBlank() || email.isBlank()) {
            _uiState.update { it.copy(error = "Veuillez remplir tous les champs du profil.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUpdatingProfile = true,
                    profileSuccessMessage = null,
                    error = null,
                    fieldErrors = emptyMap()
                )
            }
            when (val result = repository.updateProfile(name.trim(), email.trim())) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isUpdatingProfile = false,
                            profileSuccessMessage = "Profil mis à jour avec succès !",
                            user = it.user?.copy(name = name.trim(), email = email.trim()) ?: User(name = name.trim(), email = email.trim())
                        )
                    }
                    loadUserProfile()
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isUpdatingProfile = false,
                            error = if (result.fieldErrors.isEmpty()) result.message else null,
                            fieldErrors = result.fieldErrors
                        )
                    }
                }
                else -> {
                    _uiState.update { it.copy(isUpdatingProfile = false) }
                }
            }
        }
    }

    fun updatePassword(current: String, new: String, confirm: String) {
        if (current.isBlank() || new.isBlank() || confirm.isBlank()) {
            _uiState.update { it.copy(error = "Veuillez remplir tous les champs du mot de passe.") }
            return
        }
        if (new != confirm) {
            _uiState.update {
                it.copy(
                    fieldErrors = mapOf("new_password_confirmation" to listOf("Les mots de passe ne correspondent pas."))
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUpdatingPassword = true,
                    passwordSuccessMessage = null,
                    error = null,
                    fieldErrors = emptyMap()
                )
            }
            when (val result = repository.updatePassword(current, new, confirm)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isUpdatingPassword = false,
                            passwordSuccessMessage = "Mot de passe modifié avec succès !"
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isUpdatingPassword = false,
                            error = if (result.fieldErrors.isEmpty()) result.message else null,
                            fieldErrors = result.fieldErrors
                        )
                    }
                }
                else -> {
                    _uiState.update { it.copy(isUpdatingPassword = false) }
                }
            }
        }
    }

    fun clearFeedback() {
        _uiState.update {
            it.copy(
                error = null,
                profileSuccessMessage = null,
                passwordSuccessMessage = null,
                fieldErrors = emptyMap()
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }
}

