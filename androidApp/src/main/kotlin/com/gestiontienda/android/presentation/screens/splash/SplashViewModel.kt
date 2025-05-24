package com.gestiontienda.android.presentation.screens.splash

import androidx.lifecycle.ViewModel
import com.gestiontienda.android.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SplashState(
    val isAuthenticated: Boolean = false,
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        // Check if user is authenticated
        _state.value = _state.value.copy(
            isAuthenticated = authRepository.currentUser != null
        )
    }
} 
