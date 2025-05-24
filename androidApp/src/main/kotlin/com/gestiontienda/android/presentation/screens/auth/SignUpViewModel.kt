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

data class SignUpState(
    val name: String = "",
    val nameError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val isSignedUp: Boolean = false,
    val error: String? = null,
)

sealed interface SignUpEvent {
    data class NameChanged(val name: String) : SignUpEvent
    data class EmailChanged(val email: String) : SignUpEvent
    data class PasswordChanged(val password: String) : SignUpEvent
    data class ConfirmPasswordChanged(val confirmPassword: String) : SignUpEvent
    object SignUp : SignUpEvent
    object ClearError : SignUpEvent
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SignUpState())
    val state: StateFlow<SignUpState> = _state.asStateFlow()

    fun onEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.NameChanged -> {
                _state.value = _state.value.copy(
                    name = event.name,
                    nameError = validateName(event.name)
                )
            }

            is SignUpEvent.EmailChanged -> {
                _state.value = _state.value.copy(
                    email = event.email,
                    emailError = validateEmail(event.email)
                )
            }

            is SignUpEvent.PasswordChanged -> {
                _state.value = _state.value.copy(
                    password = event.password,
                    passwordError = validatePassword(event.password),
                    confirmPasswordError = validateConfirmPassword(
                        event.password,
                        _state.value.confirmPassword
                    )
                )
            }

            is SignUpEvent.ConfirmPasswordChanged -> {
                _state.value = _state.value.copy(
                    confirmPassword = event.confirmPassword,
                    confirmPasswordError = validateConfirmPassword(
                        _state.value.password,
                        event.confirmPassword
                    )
                )
            }

            SignUpEvent.SignUp -> signUp()
            SignUpEvent.ClearError -> {
                _state.value = _state.value.copy(error = null)
            }
        }
    }

    private fun signUp() {
        val nameError = validateName(_state.value.name)
        val emailError = validateEmail(_state.value.email)
        val passwordError = validatePassword(_state.value.password)
        val confirmPasswordError =
            validateConfirmPassword(_state.value.password, _state.value.confirmPassword)

        if (nameError != null || emailError != null || passwordError != null || confirmPasswordError != null) {
            _state.value = _state.value.copy(
                nameError = nameError,
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )

            when (val result = authRepository.signUp(
                email = _state.value.email,
                password = _state.value.password,
                name = _state.value.name
            )) {
                is AuthResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSignedUp = true
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

    private fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "El nombre es requerido"
            name.length < 2 -> "El nombre debe tener al menos 2 caracteres"
            else -> null
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

    private fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "La confirmación de contraseña es requerida"
            confirmPassword != password -> "Las contraseñas no coinciden"
            else -> null
        }
    }
} 
