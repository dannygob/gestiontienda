package com.gestiontienda.android.presentation.screens.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestiontienda.android.data.local.entities.SaleStatus
import com.gestiontienda.android.data.local.entities.SaleWithItems
import com.gestiontienda.android.data.repository.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

data class SalesHistoryState(
    val sales: List<SaleWithItems> = emptyList(),
    val stats: SalesStats = SalesStats(0.0, 0, 0),
    val startDate: LocalDateTime = LocalDateTime.now().minusDays(7),
    val endDate: LocalDateTime = LocalDateTime.now(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface SalesHistoryEvent {
    data class SetStartDate(val date: LocalDateTime) : SalesHistoryEvent
    data class SetEndDate(val date: LocalDateTime) : SalesHistoryEvent
    data class UpdateSaleStatus(val saleId: Long, val status: SaleStatus) : SalesHistoryEvent
    object ClearError : SalesHistoryEvent
}

@HiltViewModel
class SalesHistoryViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SalesHistoryState())
    val state: StateFlow<SalesHistoryState> = _state.asStateFlow()

    init {
        loadSalesData()
    }

    fun onEvent(event: SalesHistoryEvent) {
        when (event) {
            is SalesHistoryEvent.SetStartDate -> {
                val startOfDay = event.date.with(LocalTime.MIN)
                _state.value = _state.value.copy(startDate = startOfDay)
                loadSalesData()
            }

            is SalesHistoryEvent.SetEndDate -> {
                val endOfDay = event.date.with(LocalTime.MAX)
                _state.value = _state.value.copy(endDate = endOfDay)
                loadSalesData()
            }

            is SalesHistoryEvent.UpdateSaleStatus -> updateSaleStatus(event.saleId, event.status)
            SalesHistoryEvent.ClearError -> clearError()
        }
    }

    private fun loadSalesData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                // Combine sales and stats flows
                combine(
                    saleRepository.getSalesByDateRange(
                        _state.value.startDate,
                        _state.value.endDate
                    ),
                    saleRepository.getSalesStats(_state.value.startDate, _state.value.endDate)
                ) { sales, stats ->
                    _state.value = _state.value.copy(
                        sales = sales,
                        stats = stats,
                        isLoading = false
                    )
                }.collect()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error al cargar las ventas: ${e.message}"
                )
            }
        }
    }

    private fun updateSaleStatus(saleId: Long, status: SaleStatus) {
        viewModelScope.launch {
            saleRepository.updateSaleStatus(saleId, status)
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        error = "Error al actualizar el estado: ${e.message}"
                    )
                }
        }
    }

    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
} 
