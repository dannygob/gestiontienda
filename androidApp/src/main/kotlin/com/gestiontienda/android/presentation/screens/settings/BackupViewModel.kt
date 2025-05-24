package com.gestiontienda.android.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestiontienda.android.domain.service.BackupService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class BackupState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastBackupDate: String? = null,
    val isAutomaticBackupEnabled: Boolean = false,
    val backupIntervalHours: Int = 24,
    val showBackupDialog: Boolean = false,
    val showRestoreDialog: Boolean = false,
    val message: String? = null,
)

sealed interface BackupEvent {
    object BackupToFirebase : BackupEvent
    object RestoreFromFirebase : BackupEvent
    data class CreateLocalBackup(val file: File) : BackupEvent
    data class RestoreFromLocal(val file: File) : BackupEvent
    data class SetAutomaticBackup(val enabled: Boolean) : BackupEvent
    data class SetBackupInterval(val hours: Int) : BackupEvent
    object DismissDialog : BackupEvent
    object DismissMessage : BackupEvent
    object ShowBackupDialog : BackupEvent
    object ShowRestoreDialog : BackupEvent
}

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupService: BackupService,
) : ViewModel() {

    private val _state = MutableStateFlow(BackupState())
    val state: StateFlow<BackupState> = _state

    init {
        loadLastBackupDate()
    }

    fun onEvent(event: BackupEvent) {
        when (event) {
            is BackupEvent.BackupToFirebase -> {
                backupToFirebase()
            }

            is BackupEvent.RestoreFromFirebase -> {
                restoreFromFirebase()
            }

            is BackupEvent.CreateLocalBackup -> {
                createLocalBackup(event.file)
            }

            is BackupEvent.RestoreFromLocal -> {
                restoreFromLocal(event.file)
            }

            is BackupEvent.SetAutomaticBackup -> {
                setAutomaticBackup(event.enabled)
            }

            is BackupEvent.SetBackupInterval -> {
                setBackupInterval(event.hours)
            }

            BackupEvent.DismissDialog -> {
                _state.update {
                    it.copy(
                        showBackupDialog = false,
                        showRestoreDialog = false
                    )
                }
            }

            BackupEvent.DismissMessage -> {
                _state.update { it.copy(message = null) }
            }

            BackupEvent.ShowBackupDialog -> {
                _state.update { it.copy(showBackupDialog = true) }
            }

            BackupEvent.ShowRestoreDialog -> {
                _state.update { it.copy(showRestoreDialog = true) }
            }
        }
    }

    private fun backupToFirebase() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            backupService.backupToFirebase()
                .onSuccess {
                    loadLastBackupDate()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            message = "Respaldo creado exitosamente"
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al crear respaldo: ${e.message}"
                        )
                    }
                }
        }
    }

    private fun restoreFromFirebase() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            backupService.restoreFromFirebase()
                .onSuccess {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            message = "Respaldo restaurado exitosamente"
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al restaurar respaldo: ${e.message}"
                        )
                    }
                }
        }
    }

    private fun createLocalBackup(file: File) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            backupService.createLocalBackup(file)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            message = "Respaldo local creado exitosamente"
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al crear respaldo local: ${e.message}"
                        )
                    }
                }
        }
    }

    private fun restoreFromLocal(file: File) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            backupService.restoreFromLocal(file)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            message = "Respaldo local restaurado exitosamente"
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al restaurar respaldo local: ${e.message}"
                        )
                    }
                }
        }
    }

    private fun setAutomaticBackup(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                backupService.scheduleAutomaticBackup(_state.value.backupIntervalHours)
            } else {
                backupService.cancelAutomaticBackup()
            }
            _state.update { it.copy(isAutomaticBackupEnabled = enabled) }
        }
    }

    private fun setBackupInterval(hours: Int) {
        viewModelScope.launch {
            if (_state.value.isAutomaticBackupEnabled) {
                backupService.scheduleAutomaticBackup(hours)
            }
            _state.update { it.copy(backupIntervalHours = hours) }
        }
    }

    private fun loadLastBackupDate() {
        viewModelScope.launch {
            backupService.getLastBackupDate()?.let { timestamp ->
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val dateStr = dateFormat.format(Date(timestamp))
                _state.update { it.copy(lastBackupDate = dateStr) }
            }
        }
    }
} 
