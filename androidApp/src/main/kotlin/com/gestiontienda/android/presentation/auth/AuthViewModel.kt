package com.gestiontienda.android.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestiontienda.android.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null,
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val isLoginMode: Boolean = true,
)

sealed interface AuthEvent {
    data class EmailChanged(val email: String) : AuthEvent
    data class PasswordChanged(val password: String) : AuthEvent
    data class NameChanged(val name: String) : AuthEvent
    object ToggleAuthMode : AuthEvent
    object Submit : AuthEvent
    object ResetPassword : AuthEvent
    object DismissError : AuthEvent
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.isUserAuthenticated().collect { isAuthenticated ->
                _state.update { it.copy(isAuthenticated = isAuthenticated) }
            }
        }
    }

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.EmailChanged -> {
                _state.update { it.copy(email = event.email) }
            }

            is AuthEvent.PasswordChanged -> {
                _state.update { it.copy(password = event.password) }
            }

            is AuthEvent.NameChanged -> {
                _state.update { it.copy(name = event.name) }
            }

            AuthEvent.ToggleAuthMode -> {
                _state.update { it.copy(isLoginMode = !it.isLoginMode, error = null) }
            }

            AuthEvent.Submit -> {
                submitAuth()
            }

            AuthEvent.ResetPassword -> {
                resetPassword()
            }

            AuthEvent.DismissError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun submitAuth() {
        val currentState = state.value
        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _state.update { it.copy(error = "Por favor complete todos los campos") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val result = if (currentState.isLoginMode) {
                authRepository.signIn(currentState.email, currentState.password)
            } else {
                if (currentState.name.isBlank()) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Por favor ingrese su nombre"
                        )
                    }
                    return@launch
                }
                authRepository.signUp(currentState.email, currentState.password, currentState.name)
            }

            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false) }
                },
                onFailure = { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Error de autenticación"
                        )
                    }
                }
            )
        }
    }

    private fun resetPassword() {
        val email = state.value.email
        if (email.isBlank()) {
            _state.update { it.copy(error = "Por favor ingrese su correo electrónico") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            authRepository.resetPassword(email).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Se ha enviado un correo para restablecer su contraseña"
                        )
                    }
                },
                onFailure = { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Error al restablecer contraseña"
                        )
                    }
                }
            )
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
} 
