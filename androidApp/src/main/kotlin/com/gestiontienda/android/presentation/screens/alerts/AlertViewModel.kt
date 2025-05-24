package com.gestiontienda.android.presentation.screens.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestiontienda.android.domain.model.AlertType
import com.gestiontienda.android.domain.model.StockAlert
import com.gestiontienda.android.domain.service.StockAlertService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlertState(
    val alerts: List<StockAlert> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFilter: AlertFilter = AlertFilter.ALL,
    val showReadAlerts: Boolean = false,
)

enum class AlertFilter {
    ALL,
    LOW_STOCK,
    OUT_OF_STOCK,
    EXPIRING,
    EXPIRED
}

sealed interface AlertEvent {
    object LoadAlerts : AlertEvent
    data class SetFilter(val filter: AlertFilter) : AlertEvent
    data class MarkAsRead(val alertId: Long) : AlertEvent
    object MarkAllAsRead : AlertEvent
    data class DeleteAlert(val alert: StockAlert) : AlertEvent
    data class ToggleShowRead(val show: Boolean) : AlertEvent
    object DismissError : AlertEvent
    object RefreshAlerts : AlertEvent
}

@HiltViewModel
class AlertViewModel @Inject constructor(
    private val alertService: StockAlertService,
) : ViewModel() {

    private val _state = MutableStateFlow(AlertState())
    val state: StateFlow<AlertState> = _state.asStateFlow()

    init {
        loadAlerts()
    }

    fun onEvent(event: AlertEvent) {
        when (event) {
            AlertEvent.LoadAlerts -> loadAlerts()
            is AlertEvent.SetFilter -> setFilter(event.filter)
            is AlertEvent.MarkAsRead -> markAsRead(event.alertId)
            AlertEvent.MarkAllAsRead -> markAllAsRead()
            is AlertEvent.DeleteAlert -> deleteAlert(event.alert)
            is AlertEvent.ToggleShowRead -> toggleShowRead(event.show)
            AlertEvent.DismissError -> dismissError()
            AlertEvent.RefreshAlerts -> refreshAlerts()
        }
    }

    private fun loadAlerts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            alertService.getAllAlerts()
                .catch { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar alertas: ${e.message}"
                        )
                    }
                }
                .collect { alerts ->
                    _state.update { state ->
                        state.copy(
                            alerts = filterAlerts(
                                alerts,
                                state.selectedFilter,
                                state.showReadAlerts
                            ),
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun setFilter(filter: AlertFilter) {
        _state.update { state ->
            state.copy(
                selectedFilter = filter,
                alerts = filterAlerts(state.alerts, filter, state.showReadAlerts)
            )
        }
    }

    private fun markAsRead(alertId: Long) {
        viewModelScope.launch {
            try {
                alertService.markAsRead(alertId)
                refreshAlerts()
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = "Error al marcar alerta como leída: ${e.message}")
                }
            }
        }
    }

    private fun markAllAsRead() {
        viewModelScope.launch {
            try {
                alertService.markAllAsRead()
                refreshAlerts()
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = "Error al marcar alertas como leídas: ${e.message}")
                }
            }
        }
    }

    private fun deleteAlert(alert: StockAlert) {
        viewModelScope.launch {
            try {
                alertService.deleteAlert(alert)
                refreshAlerts()
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = "Error al eliminar alerta: ${e.message}")
                }
            }
        }
    }

    private fun toggleShowRead(show: Boolean) {
        _state.update { state ->
            state.copy(
                showReadAlerts = show,
                alerts = filterAlerts(state.alerts, state.selectedFilter, show)
            )
        }
    }

    private fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    private fun refreshAlerts() {
        alertService.checkLowStockProducts()
        alertService.checkExpiringProducts()
        loadAlerts()
    }

    private fun filterAlerts(
        alerts: List<StockAlert>,
        filter: AlertFilter,
        showRead: Boolean,
    ): List<StockAlert> {
        var filtered = when (filter) {
            AlertFilter.ALL -> alerts
            AlertFilter.LOW_STOCK -> alerts.filter { it.alertType == AlertType.LOW_STOCK }
            AlertFilter.OUT_OF_STOCK -> alerts.filter { it.alertType == AlertType.OUT_OF_STOCK }
            AlertFilter.EXPIRING -> alerts.filter { it.alertType == AlertType.EXPIRING_SOON }
            AlertFilter.EXPIRED -> alerts.filter { it.alertType == AlertType.EXPIRED }
        }

        if (!showRead) {
            filtered = filtered.filter { !it.isRead }
        }

        return filtered.sortedByDescending { it.createdAt }
    }
} 
