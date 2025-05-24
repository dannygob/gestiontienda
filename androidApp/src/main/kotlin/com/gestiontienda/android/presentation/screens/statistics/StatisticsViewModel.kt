package com.gestiontienda.android.presentation.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestiontienda.android.domain.model.ProductSalesStats
import com.gestiontienda.android.domain.model.SalesStatistics
import com.gestiontienda.android.domain.model.StatisticsPeriod
import com.gestiontienda.android.domain.service.ExportFormat
import com.gestiontienda.android.domain.service.ReportFormat
import com.gestiontienda.android.domain.service.SalesStatisticsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class StatisticsState(
    val statistics: SalesStatistics? = null,
    val selectedPeriod: StatisticsPeriod = StatisticsPeriod.DAILY,
    val startDate: Date? = null,
    val endDate: Date? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedProductId: Long? = null,
    val selectedProductStats: ProductSalesStats? = null,
    val exportPath: String? = null,
)

sealed interface StatisticsEvent {
    data class SelectPeriod(val period: StatisticsPeriod) : StatisticsEvent
    data class SetDateRange(val start: Date?, val end: Date?) : StatisticsEvent
    data class SelectProduct(val productId: Long) : StatisticsEvent
    data class ExportData(val format: ExportFormat) : StatisticsEvent
    data class GenerateReport(val format: ReportFormat) : StatisticsEvent
    object RefreshStatistics : StatisticsEvent
    object ClearError : StatisticsEvent
    object ClearExportPath : StatisticsEvent
}

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statisticsService: SalesStatisticsService,
) : ViewModel() {

    private val _state = MutableStateFlow(StatisticsState())
    val state: StateFlow<StatisticsState> = _state.asStateFlow()

    init {
        loadStatistics()
    }

    fun onEvent(event: StatisticsEvent) {
        when (event) {
            is StatisticsEvent.SelectPeriod -> selectPeriod(event.period)
            is StatisticsEvent.SetDateRange -> setDateRange(event.start, event.end)
            is StatisticsEvent.SelectProduct -> selectProduct(event.productId)
            is StatisticsEvent.ExportData -> exportData(event.format)
            is StatisticsEvent.GenerateReport -> generateReport(event.format)
            StatisticsEvent.RefreshStatistics -> loadStatistics()
            StatisticsEvent.ClearError -> clearError()
            StatisticsEvent.ClearExportPath -> clearExportPath()
        }
    }

    private fun selectPeriod(period: StatisticsPeriod) {
        _state.update {
            it.copy(
                selectedPeriod = period,
                startDate = null,
                endDate = null
            )
        }
        loadStatistics()
    }

    private fun setDateRange(start: Date?, end: Date?) {
        _state.update {
            it.copy(
                startDate = start,
                endDate = end,
                selectedPeriod = StatisticsPeriod.CUSTOM
            )
        }
        loadStatistics()
    }

    private fun selectProduct(productId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val stats = statisticsService.getProductStatistics(
                    productId = productId,
                    period = state.value.selectedPeriod,
                    startDate = state.value.startDate,
                    endDate = state.value.endDate
                )
                _state.update {
                    it.copy(
                        selectedProductId = productId,
                        selectedProductStats = stats,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Error al cargar estadísticas del producto: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val stats = statisticsService.getSalesStatistics(
                    period = state.value.selectedPeriod,
                    startDate = state.value.startDate,
                    endDate = state.value.endDate
                )
                _state.update {
                    it.copy(
                        statistics = stats,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Error al cargar estadísticas: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun exportData(format: ExportFormat) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val path = statisticsService.exportSalesData(
                    startDate = state.value.startDate,
                    endDate = state.value.endDate,
                    format = format
                )
                _state.update {
                    it.copy(
                        exportPath = path,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Error al exportar datos: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun generateReport(format: ReportFormat) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val path = statisticsService.generateSalesReport(
                    period = state.value.selectedPeriod,
                    startDate = state.value.startDate,
                    endDate = state.value.endDate,
                    format = format
                )
                _state.update {
                    it.copy(
                        exportPath = path,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Error al generar reporte: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun clearExportPath() {
        _state.update { it.copy(exportPath = null) }
    }
} 
