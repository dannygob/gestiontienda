package com.gestiontienda.android.presentation.screens.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestiontienda.android.data.local.dao.ProductDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlertsState(
    val alerts: List<StockAlert> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val productDao: ProductDao,
) : ViewModel() {

    private val _state = MutableStateFlow(AlertsState())
    val state: StateFlow<AlertsState> = _state

    init {
        refreshAlerts()
    }

    fun refreshAlerts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                productDao.getAllProducts()
                    .catch { e ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = e.message
                        )
                    }
                    .collect { products ->
                        val alerts = products
                            .filter { product ->
                                product.stock <= product.minStock
                            }
                            .map { product ->
                                StockAlert(
                                    product = product,
                                    type = if (product.stock == 0) {
                                        AlertType.OUT_OF_STOCK
                                    } else {
                                        AlertType.LOW_STOCK
                                    }
                                )
                            }
                            .sortedWith(
                                compareBy<StockAlert> {
                                    when (it.type) {
                                        AlertType.OUT_OF_STOCK -> 0
                                        AlertType.LOW_STOCK -> 1
                                    }
                                }.thenBy {
                                    it.product.stock
                                }
                            )

                        _state.value = _state.value.copy(
                            alerts = alerts,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error al cargar las alertas: ${e.message}"
                )
            }
        }
    }
} 
