package com.gestiontienda.android.presentation.screens.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestiontienda.android.data.local.entities.ProductEntity
import com.gestiontienda.android.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventoryState(
    val products: List<ProductEntity> = emptyList(),
    val categories: List<String> = listOf("Sin Categoría"),
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedProduct: ProductEntity? = null,
    val showScanner: Boolean = false,
    val showAddDialog: Boolean = false,
)

sealed interface InventoryEvent {
    data class SearchQueryChanged(val query: String) : InventoryEvent
    object Search : InventoryEvent
    object ClearSearch : InventoryEvent
    object ToggleSearchBar : InventoryEvent
    data class SelectProduct(val product: ProductEntity) : InventoryEvent
    object ToggleScanner : InventoryEvent
    data class OnBarcodeDetected(val barcode: String) : InventoryEvent
    object DismissDialog : InventoryEvent
    data class AddProduct(val product: ProductEntity) : InventoryEvent
    data class UpdateProduct(val product: ProductEntity) : InventoryEvent
    data class SelectCategory(val category: String?) : InventoryEvent
    data class AddCategory(val category: String) : InventoryEvent
}

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(InventoryState())
    val state: StateFlow<InventoryState> = _state.asStateFlow()

    init {
        loadProducts()
        loadCategories()
    }

    fun onEvent(event: InventoryEvent) {
        when (event) {
            is InventoryEvent.SearchQueryChanged -> {
                _state.value = _state.value.copy(searchQuery = event.query)
                searchProducts(event.query)
            }

            InventoryEvent.Search -> {
                searchProducts(_state.value.searchQuery)
            }

            InventoryEvent.ClearSearch -> {
                _state.value = _state.value.copy(
                    searchQuery = "",
                    isSearchActive = false
                )
                loadProducts()
            }

            InventoryEvent.ToggleSearchBar -> {
                _state.value = _state.value.copy(
                    isSearchActive = !_state.value.isSearchActive,
                    searchQuery = if (_state.value.isSearchActive) "" else _state.value.searchQuery
                )
                if (!_state.value.isSearchActive) {
                    loadProducts()
                }
            }

            is InventoryEvent.SelectProduct -> {
                _state.value = _state.value.copy(
                    selectedProduct = event.product,
                    showAddDialog = true
                )
            }

            InventoryEvent.ToggleScanner -> {
                _state.value = _state.value.copy(
                    showScanner = !_state.value.showScanner,
                    showAddDialog = false
                )
            }

            is InventoryEvent.OnBarcodeDetected -> {
                handleBarcodeDetected(event.barcode)
            }

            InventoryEvent.DismissDialog -> {
                _state.value = _state.value.copy(
                    showAddDialog = false,
                    selectedProduct = null
                )
            }

            is InventoryEvent.AddProduct -> {
                viewModelScope.launch {
                    try {
                        productRepository.insertProduct(event.product)
                        _state.value = _state.value.copy(
                            showAddDialog = false,
                            selectedProduct = null
                        )
                        loadProducts()
                        loadCategories()
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            error = "Error al agregar producto: ${e.message}"
                        )
                    }
                }
            }

            is InventoryEvent.UpdateProduct -> {
                viewModelScope.launch {
                    try {
                        productRepository.updateProduct(event.product)
                        _state.value = _state.value.copy(
                            showAddDialog = false,
                            selectedProduct = null
                        )
                        loadProducts()
                        loadCategories()
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            error = "Error al actualizar producto: ${e.message}"
                        )
                    }
                }
            }

            is InventoryEvent.SelectCategory -> {
                _state.value = _state.value.copy(selectedCategory = event.category)
                loadProducts()
            }

            is InventoryEvent.AddCategory -> {
                val currentCategories = _state.value.categories.toMutableList()
                if (!currentCategories.contains(event.category)) {
                    currentCategories.add(event.category)
                    _state.value = _state.value.copy(categories = currentCategories)
                }
            }
        }
    }

    private fun handleBarcodeDetected(barcode: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                val product = productRepository.getProductByBarcode(barcode)
                _state.value = _state.value.copy(
                    showScanner = false,
                    showAddDialog = true,
                    selectedProduct = product ?: ProductEntity(
                        barcode = barcode,
                        name = "",
                        price = 0.0,
                        stock = 0
                    ),
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error al buscar producto: ${e.message}"
                )
            }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                productRepository.getAllProducts()
                    .collect { products ->
                        val filteredProducts = when {
                            _state.value.selectedCategory != null -> {
                                products.filter { it.category == _state.value.selectedCategory }
                            }

                            _state.value.searchQuery.isNotBlank() -> {
                                products.filter {
                                    it.name.contains(_state.value.searchQuery, ignoreCase = true) ||
                                            it.barcode?.contains(
                                                _state.value.searchQuery,
                                                ignoreCase = true
                                            ) == true
                                }
                            }

                            else -> products
                        }
                        _state.value = _state.value.copy(
                            products = filteredProducts,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error al cargar productos: ${e.message}"
                )
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                productRepository.getAllProducts()
                    .collect { products ->
                        val categories = products
                            .map { it.category }
                            .distinct()
                            .sorted()
                            .toMutableList()
                        if (!categories.contains("Sin Categoría")) {
                            categories.add(0, "Sin Categoría")
                        }
                        _state.value = _state.value.copy(categories = categories)
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Error al cargar categorías: ${e.message}"
                )
            }
        }
    }

    private fun searchProducts(query: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                productRepository.searchProducts(query)
                    .collect { products ->
                        val filteredProducts = if (_state.value.selectedCategory != null) {
                            products.filter { it.category == _state.value.selectedCategory }
                        } else {
                            products
                        }
                        _state.value = _state.value.copy(
                            products = filteredProducts,
                            isLoading = false
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
} 
