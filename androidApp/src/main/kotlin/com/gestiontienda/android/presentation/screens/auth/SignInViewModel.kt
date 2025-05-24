package com.gestiontienda.android.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.gestiontienda.android.data.repository.AuthRepository
import com.gestiontienda.android.data.repository.AuthResult
import com.gestiontienda.android.data.sync.DataSyncService
import com.gestiontienda.android.data.sync.DataSyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignInState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    val error: String? = null,
    val isSyncing: Boolean = false,
    val syncMessage: String? = null,
)

sealed interface SignInEvent {
    data class EmailChanged(val email: String) : SignInEvent
    data class PasswordChanged(val password: String) : SignInEvent
    object SignIn : SignInEvent
    object ClearError : SignInEvent
}

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val dataSyncService: DataSyncService,
    private val workManager: WorkManager,
) : ViewModel() {

    private val _state = MutableStateFlow(SignInState())
    val state: StateFlow<SignInState> = _state.asStateFlow()

    init {
        // Check if user is already signed in
        authRepository.currentUser?.let {
            _state.value = _state.value.copy(isSignedIn = true)
            setupDataSync()
        }
    }

    fun onEvent(event: SignInEvent) {
        when (event) {
            is SignInEvent.EmailChanged -> {
                _state.value = _state.value.copy(
                    email = event.email,
                    emailError = validateEmail(event.email)
                )
            }

            is SignInEvent.PasswordChanged -> {
                _state.value = _state.value.copy(
                    password = event.password,
                    passwordError = validatePassword(event.password)
                )
            }

            SignInEvent.SignIn -> signIn()
            SignInEvent.ClearError -> {
                _state.value = _state.value.copy(error = null)
            }
        }
    }

    private fun signIn() {
        val emailError = validateEmail(_state.value.email)
        val passwordError = validatePassword(_state.value.password)

        if (emailError != null || passwordError != null) {
            _state.value = _state.value.copy(
                emailError = emailError,
                passwordError = passwordError
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )

            when (val result = authRepository.signIn(_state.value.email, _state.value.password)) {
                is AuthResult.Success -> {
                    setupDataSync()
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSignedIn = true
                    )
                }

                is AuthResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    private fun setupDataSync() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isSyncing = true,
                    syncMessage = "Sincronizando datos..."
                )

                // Perform initial sync
                dataSyncService.performInitialSync()

                // Schedule periodic sync
                DataSyncWorker.schedule(workManager)

                _state.value = _state.value.copy(
                    isSyncing = false,
                    syncMessage = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSyncing = false,
                    syncMessage = null,
                    error = "Error al sincronizar datos: ${e.message}"
                )
            }
        }
    }

    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "El correo electrónico es requerido"
            !email.contains("@") -> "El correo electrónico no es válido"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "La contraseña es requerida"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }
    }
} 
