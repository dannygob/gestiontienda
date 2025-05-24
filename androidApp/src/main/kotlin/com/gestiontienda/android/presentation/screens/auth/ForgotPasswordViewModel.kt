package com.gestiontienda.android.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestiontienda.android.data.repository.AuthRepository
import com.gestiontienda.android.data.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForgotPasswordState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val isEmailSent: Boolean = false,
    val error: String? = null,
)

sealed interface ForgotPasswordEvent {
    data class EmailChanged(val email: String) : ForgotPasswordEvent
    object SendResetEmail : ForgotPasswordEvent
    object ClearError : ForgotPasswordEvent
}

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    fun onEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.EmailChanged -> {
                _state.value = _state.value.copy(
                    email = event.email,
                    emailError = validateEmail(event.email)
                )
            }

            ForgotPasswordEvent.SendResetEmail -> sendResetEmail()
            ForgotPasswordEvent.ClearError -> {
                _state.value = _state.value.copy(error = null)
            }
        }
    }

    private fun sendResetEmail() {
        val emailError = validateEmail(_state.value.email)

        if (emailError != null) {
            _state.value = _state.value.copy(emailError = emailError)
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )

            when (val result = authRepository.resetPassword(_state.value.email)) {
                is AuthResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isEmailSent = true
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

    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "El correo electrónico es requerido"
            !email.contains("@") -> "El correo electrónico no es válido"
            else -> null
        }
    }
} 
