package com.gestiontienda.android.presentation.screens.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestiontienda.android.domain.model.Customer
import com.gestiontienda.android.domain.model.CustomerStatus
import com.gestiontienda.android.domain.service.CustomerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomersState(
    val customers: List<Customer> = emptyList(),
    val searchQuery: String = "",
    val selectedStatus: CustomerStatus = CustomerStatus.ACTIVE,
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface CustomersEvent {
    data class SearchQueryChanged(val query: String) : CustomersEvent
    data class StatusFilterChanged(val status: CustomerStatus) : CustomersEvent
    data class AddCustomer(val customer: Customer) : CustomersEvent
    object ClearError : CustomersEvent
}

@HiltViewModel
class CustomersViewModel @Inject constructor(
    private val customerService: CustomerService,
) : ViewModel() {

    private val _state = MutableStateFlow(CustomersState())
    val state: StateFlow<CustomersState> = _state.asStateFlow()

    private val searchQuery = MutableStateFlow("")
    private val selectedStatus = MutableStateFlow(CustomerStatus.ACTIVE)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val customersFlow = combine(
        searchQuery,
        selectedStatus
    ) { query, status ->
        Pair(query, status)
    }.flatMapLatest { (query, status) ->
        if (query.isBlank()) {
            customerService.getCustomersByStatus(status)
        } else {
            customerService.searchCustomers(query)
                .map { customers -> customers.filter { it.status == status } }
        }
    }

    init {
        viewModelScope.launch {
            customersFlow.collect { customers ->
                _state.update { it.copy(customers = customers) }
            }
        }

        viewModelScope.launch {
            searchQuery.collect { query ->
                _state.update { it.copy(searchQuery = query) }
            }
        }

        viewModelScope.launch {
            selectedStatus.collect { status ->
                _state.update { it.copy(selectedStatus = status) }
            }
        }
    }

    fun onEvent(event: CustomersEvent) {
        when (event) {
            is CustomersEvent.SearchQueryChanged -> {
                searchQuery.value = event.query
            }

            is CustomersEvent.StatusFilterChanged -> {
                selectedStatus.value = event.status
            }

            is CustomersEvent.AddCustomer -> {
                viewModelScope.launch {
                    try {
                        _state.update { it.copy(isLoading = true) }
                        customerService.createCustomer(event.customer)
                    } catch (e: Exception) {
                        _state.update {
                            it.copy(error = "Error al crear cliente: ${e.message}")
                        }
                    } finally {
                        _state.update { it.copy(isLoading = false) }
                    }
                }
            }

            CustomersEvent.ClearError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }
} 
