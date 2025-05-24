package com.gestiontienda.android.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestiontienda.android.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class MainState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = false,
    val userEmail: String? = null,
    val userName: String? = null,
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.isUserAuthenticated()
                .onEach { isAuthenticated ->
                    _state.update {
                        it.copy(
                            isAuthenticated = isAuthenticated,
                            userEmail = if (isAuthenticated) authRepository.getCurrentUserEmail() else null,
                            userName = if (isAuthenticated) authRepository.getCurrentUserName() else null,
                            isLoading = false
                        )
                    }
                }
                .catch { e ->
                    _state.update {
                        it.copy(
                            isAuthenticated = false,
                            isLoading = false
                        )
                    }
                }
                .collect()
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
} 
