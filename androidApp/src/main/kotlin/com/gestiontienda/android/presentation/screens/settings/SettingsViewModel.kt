package com.gestiontienda.android.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestiontienda.android.data.local.entities.Permission
import com.gestiontienda.android.data.local.entities.UserRole
import com.gestiontienda.android.domain.model.StoreConfig
import com.gestiontienda.android.domain.model.UserWithRole
import com.gestiontienda.android.domain.repository.AuthRepository
import com.gestiontienda.android.domain.repository.ConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val storeConfig: StoreConfig? = null,
    val users: List<UserWithRole> = emptyList(),
    val hasManageUsersPermission: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface SettingsEvent {
    data class UpdateStoreConfig(val config: StoreConfig) : SettingsEvent
    data class AddUser(val email: String, val name: String, val role: UserRole) : SettingsEvent
    data class UpdateUserRole(val userId: String, val role: UserRole) : SettingsEvent
    data class DeleteUser(val userId: String) : SettingsEvent
    object ClearError : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // Load store config
            configRepository.getStoreConfig()
                .onEach { config ->
                    _state.update { it.copy(storeConfig = config) }
                }
                .catch { e ->
                    _state.update {
                        it.copy(error = "Error al cargar configuración: ${e.message}")
                    }
                }
                .launchIn(viewModelScope)

            // Load users
            configRepository.getAllUsers()
                .onEach { users ->
                    _state.update { it.copy(users = users) }
                }
                .catch { e ->
                    _state.update {
                        it.copy(error = "Error al cargar usuarios: ${e.message}")
                    }
                }
                .launchIn(viewModelScope)

            // Check permissions
            configRepository.hasPermission(Permission.MANAGE_USERS)
                .onEach { hasPermission ->
                    _state.update { it.copy(hasManageUsersPermission = hasPermission) }
                }
                .catch { e ->
                    _state.update {
                        it.copy(error = "Error al verificar permisos: ${e.message}")
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.UpdateStoreConfig -> {
                viewModelScope.launch {
                    try {
                        _state.update { it.copy(isLoading = true) }
                        configRepository.updateStoreConfig(event.config)
                        _state.update { it.copy(isLoading = false) }
                    } catch (e: Exception) {
                        _state.update {
                            it.copy(
                                error = "Error al actualizar configuración: ${e.message}",
                                isLoading = false
                            )
                        }
                    }
                }
            }

            is SettingsEvent.AddUser -> {
                viewModelScope.launch {
                    try {
                        _state.update { it.copy(isLoading = true) }
                        authRepository.signUp(event.email, "password123", event.name).fold(
                            onSuccess = { user ->
                                configRepository.updateUserRole(user.id, event.role)
                                _state.update { it.copy(isLoading = false) }
                            },
                            onFailure = { e ->
                                _state.update {
                                    it.copy(
                                        error = "Error al crear usuario: ${e.message}",
                                        isLoading = false
                                    )
                                }
                            }
                        )
                    } catch (e: Exception) {
                        _state.update {
                            it.copy(
                                error = "Error al crear usuario: ${e.message}",
                                isLoading = false
                            )
                        }
                    }
                }
            }

            is SettingsEvent.UpdateUserRole -> {
                viewModelScope.launch {
                    try {
                        _state.update { it.copy(isLoading = true) }
                        configRepository.updateUserRole(event.userId, event.role)
                        _state.update { it.copy(isLoading = false) }
                    } catch (e: Exception) {
                        _state.update {
                            it.copy(
                                error = "Error al actualizar rol: ${e.message}",
                                isLoading = false
                            )
                        }
                    }
                }
            }

            is SettingsEvent.DeleteUser -> {
                viewModelScope.launch {
                    try {
                        _state.update { it.copy(isLoading = true) }
                        configRepository.deleteUserRole(event.userId)
                        _state.update { it.copy(isLoading = false) }
                    } catch (e: Exception) {
                        _state.update {
                            it.copy(
                                error = "Error al eliminar usuario: ${e.message}",
                                isLoading = false
                            )
                        }
                    }
                }
            }

            SettingsEvent.ClearError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }
} 
