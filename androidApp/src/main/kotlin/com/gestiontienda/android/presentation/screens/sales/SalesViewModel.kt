package com.gestiontienda.android.presentation.screens.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestiontienda.android.data.local.dao.SaleWithSummary
import com.gestiontienda.android.data.local.dao.SalesSummary
import com.gestiontienda.android.data.local.entities.ProductEntity
import com.gestiontienda.android.data.local.entities.SaleEntity
import com.gestiontienda.android.data.local.entities.SaleItemEntity
import com.gestiontienda.android.domain.repository.ProductRepository
import com.gestiontienda.android.domain.repository.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class StockValidationError(
    val productName: String,
    val requested: Int,
    val available: Int,
)

data class SalesState(
    val recentSales: List<SaleWithSummary> = emptyList(),
    val salesSummary: SalesSummary? = null,
    val currentSaleItems: List<SaleItem> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val selectedProduct: ProductEntity? = null,
    val showAddDialog: Boolean = false,
    val showSaleDialog: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val stockErrors: List<StockValidationError> = emptyList(),
    val dateRange: DateRange = DateRange.TODAY,
)

data class SaleItem(
    val product: ProductEntity,
    val quantity: Int,
    val priceAtSale: Double,
    val discount: Double = 0.0,
)

enum class DateRange {
    TODAY,
    LAST_WEEK,
    LAST_MONTH,
    CUSTOM
}

sealed interface SalesEvent {
    data class SearchQueryChanged(val query: String) : SalesEvent
    object ToggleSearchBar : SalesEvent
    data class SelectProduct(val product: ProductEntity) : SalesEvent
    data class UpdateQuantity(val product: ProductEntity, val quantity: Int) : SalesEvent
    data class UpdateDiscount(val product: ProductEntity, val discount: Double) : SalesEvent
    data class RemoveProduct(val product: ProductEntity) : SalesEvent
    object ClearCart : SalesEvent
    data class CompleteSale(
        val paymentMethod: String,
        val customerName: String?,
        val notes: String?,
    ) : SalesEvent

    data class SetDateRange(val range: DateRange) : SalesEvent
    object DismissDialog : SalesEvent
}

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SalesState())
    val state: StateFlow<SalesState> = _state.asStateFlow()

    init {
        loadRecentSales()
        loadSalesSummary()
    }

    fun onEvent(event: SalesEvent) {
        when (event) {
            is SalesEvent.SearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
                searchProducts(event.query)
            }

            SalesEvent.ToggleSearchBar -> {
                _state.update {
                    it.copy(
                        isSearchActive = !_state.value.isSearchActive,
                        searchQuery = if (_state.value.isSearchActive) "" else _state.value.searchQuery
                    )
                }
            }

            is SalesEvent.SelectProduct -> {
                val existingItem =
                    _state.value.currentSaleItems.find { it.product.id == event.product.id }
                if (existingItem != null) {
                    updateQuantity(event.product, existingItem.quantity + 1)
                } else {
                    _state.update {
                        it.copy(
                            currentSaleItems = it.currentSaleItems + SaleItem(
                                product = event.product,
                                quantity = 1,
                                priceAtSale = event.product.price
                            )
                        )
                    }
                }
            }

            is SalesEvent.UpdateQuantity -> {
                updateQuantity(event.product, event.quantity)
            }

            is SalesEvent.UpdateDiscount -> {
                updateSaleItem(event.product) { it.copy(discount = event.discount) }
            }

            is SalesEvent.RemoveProduct -> {
                _state.update {
                    it.copy(
                        currentSaleItems = it.currentSaleItems.filter { item ->
                            item.product.id != event.product.id
                        }
                    )
                }
            }

            SalesEvent.ClearCart -> {
                _state.update { it.copy(currentSaleItems = emptyList()) }
            }

            is SalesEvent.CompleteSale -> {
                completeSale(event.paymentMethod, event.customerName, event.notes)
            }

            is SalesEvent.SetDateRange -> {
                _state.update { it.copy(dateRange = event.range) }
                loadSalesSummary()
            }

            SalesEvent.DismissDialog -> {
                _state.update {
                    it.copy(
                        showAddDialog = false,
                        showSaleDialog = false,
                        selectedProduct = null,
                        stockErrors = emptyList()
                    )
                }
            }
        }
    }

    private fun updateQuantity(product: ProductEntity, newQuantity: Int) {
        if (newQuantity <= 0) {
            onEvent(SalesEvent.RemoveProduct(product))
            return
        }

        viewModelScope.launch {
            val currentStock = productRepository.getProduct(product.id)?.stock ?: 0
            if (newQuantity > currentStock) {
                _state.update {
                    it.copy(
                        error = "Stock insuficiente para ${product.name}. Disponible: $currentStock"
                    )
                }
                return@launch
            }

            updateSaleItem(product) { it.copy(quantity = newQuantity) }
        }
    }

    private suspend fun validateStock(): List<StockValidationError> {
        val errors = mutableListOf<StockValidationError>()

        state.value.currentSaleItems.forEach { item ->
            val currentStock = productRepository.getProduct(item.product.id)?.stock ?: 0
            if (item.quantity > currentStock) {
                errors.add(
                    StockValidationError(
                        productName = item.product.name,
                        requested = item.quantity,
                        available = currentStock
                    )
                )
            }
        }

        return errors
    }

    private fun updateSaleItem(product: ProductEntity, update: (SaleItem) -> SaleItem) {
        _state.value = _state.value.copy(
            currentSaleItems = _state.value.currentSaleItems.map {
                if (it.product.id == product.id) update(it) else it
            }
        )
    }

    private fun searchProducts(query: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                productRepository.searchProducts(query)
                    .collect { products ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            selectedProduct = if (products.isNotEmpty()) products.first() else null
                        )
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error al buscar productos: ${e.message}"
                )
            }
        }
    }

    private fun completeSale(
        paymentMethod: String,
        customerName: String?,
        notes: String?,
    ) {
        if (state.value.currentSaleItems.isEmpty()) {
            _state.update { it.copy(error = "El carrito está vacío") }
            return
        }

        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }

                // Validate stock before proceeding
                val stockErrors = validateStock()
                if (stockErrors.isNotEmpty()) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            stockErrors = stockErrors,
                            error = "Stock insuficiente para algunos productos"
                        )
                    }
                    return@launch
                }

                val total = state.value.currentSaleItems.sumOf { item ->
                    (item.priceAtSale * (1 - item.discount)) * item.quantity
                }

                val sale = SaleEntity(
                    total = total,
                    paymentMethod = paymentMethod,
                    customerName = customerName,
                    notes = notes
                )

                val saleItems = state.value.currentSaleItems.map { item ->
                    SaleItemEntity(
                        saleId = 0, // Will be set by Room
                        productId = item.product.id,
                        quantity = item.quantity,
                        priceAtSale = item.priceAtSale,
                        discount = item.discount
                    )
                }

                // Update product stock
                saleItems.forEach { item ->
                    val product = productRepository.getProduct(item.productId)
                    if (product != null) {
                        productRepository.updateProduct(
                            product.copy(stock = product.stock - item.quantity)
                        )
                    }
                }

                saleRepository.createSale(sale, saleItems)

                _state.update {
                    it.copy(
                        currentSaleItems = emptyList(),
                        showSaleDialog = false,
                        isLoading = false,
                        stockErrors = emptyList()
                    )
                }

                loadRecentSales()
                loadSalesSummary()
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al completar la venta: ${e.message}"
                    )
                }
            }
        }
    }

    private fun loadRecentSales() {
        viewModelScope.launch {
            try {
                saleRepository.getRecentSales(10)
                    .collect { sales ->
                        _state.value = _state.value.copy(recentSales = sales)
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Error al cargar ventas recientes: ${e.message}"
                )
            }
        }
    }

    private fun loadSalesSummary() {
        viewModelScope.launch {
            try {
                val (startDate, endDate) = getDateRange(_state.value.dateRange)
                saleRepository.getSalesSummary(startDate, endDate)
                    .collect { summary ->
                        _state.value = _state.value.copy(salesSummary = summary)
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Error al cargar resumen de ventas: ${e.message}"
                )
            }
        }
    }

    private fun getDateRange(range: DateRange): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startDate = when (range) {
            DateRange.TODAY -> calendar.timeInMillis
            DateRange.LAST_WEEK -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                calendar.timeInMillis
            }

            DateRange.LAST_MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.timeInMillis
            }

            DateRange.CUSTOM -> calendar.timeInMillis // Custom range should be handled separately
        }

        return startDate to endDate
    }
} 
