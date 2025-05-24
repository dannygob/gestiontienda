package com.gestiontienda.android.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.gestiontienda.android.data.repository.AuthRepository
import com.gestiontienda.android.data.sync.DataSyncService
import com.gestiontienda.android.data.sync.DataSyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val isSyncing: Boolean = false,
    val syncMessage: String? = null,
    val error: String? = null,
    val isSignedOut: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val dataSyncService: DataSyncService,
    private val workManager: WorkManager,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    fun signOut() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isSyncing = true,
                    syncMessage = "Sincronizando datos finales..."
                )

                // Perform final sync before signing out
                dataSyncService.syncToCloud()

                // Cancel periodic sync
                DataSyncWorker.cancel(workManager)

                // Sign out
                authRepository.signOut()

                _state.value = _state.value.copy(
                    isSyncing = false,
                    syncMessage = null,
                    isSignedOut = true
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSyncing = false,
                    syncMessage = null,
                    error = "Error al cerrar sesión: ${e.message}"
                )
            }
        }
    }
} 
