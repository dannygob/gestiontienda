package com.gestiontienda.android.presentation.screens.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestiontienda.android.domain.model.Customer
import com.gestiontienda.android.domain.model.CustomerCredit
import com.gestiontienda.android.domain.service.CustomerService
import com.gestiontienda.android.domain.service.CustomerStatistics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerDetailState(
    val customer: Customer? = null,
    val credits: List<CustomerCredit> = emptyList(),
    val statistics: CustomerStatistics? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface CustomerDetailEvent {
    data class UpdateCustomer(val customer: Customer) : CustomerDetailEvent
    data class AddCredit(val credit: CustomerCredit) : CustomerDetailEvent
    data class ProcessPayment(val creditId: Long, val amount: Double) : CustomerDetailEvent
    data class DeleteCustomer(val customer: Customer) : CustomerDetailEvent
    object ClearError : CustomerDetailEvent
}

@HiltViewModel
class CustomerDetailViewModel @Inject constructor(
    private val customerService: CustomerService,
) : ViewModel() {

    private val _state = MutableStateFlow(CustomerDetailState())
    val state: StateFlow<CustomerDetailState> = _state.asStateFlow()

    fun loadCustomer(customerId: Long) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                // Load customer details
                val customer = customerService.getCustomerById(customerId)
                if (customer == null) {
                    _state.update {
                        it.copy(
                            error = "Cliente no encontrado",
                            isLoading = false
                        )
                    }
                    return@launch
                }

                // Start collecting customer credits
                launch {
                    customerService.getCustomerCredits(customerId)
                        .collect { credits ->
                            _state.update { it.copy(credits = credits) }
                        }
                }

                // Start collecting customer statistics
                launch {
                    customerService.getCustomerStatistics(customerId)
                        .collect { stats ->
                            _state.update { it.copy(statistics = stats) }
                        }
                }

                _state.update {
                    it.copy(
                        customer = customer,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Error al cargar cliente: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onEvent(event: CustomerDetailEvent) {
        when (event) {
            is CustomerDetailEvent.UpdateCustomer -> {
                viewModelScope.launch {
                    try {
                        _state.update { it.copy(isLoading = true) }
                        customerService.updateCustomer(event.customer)
                        _state.update {
                            it.copy(
                                customer = event.customer,
                                isLoading = false
                            )
                        }
                    } catch (e: Exception) {
                        _state.update {
                            it.copy(
                                error = "Error al actualizar cliente: ${e.message}",
                                isLoading = false
                            )
                        }
                    }
                }
            }

            is CustomerDetailEvent.AddCredit -> {
                viewModelScope.launch {
                    try {
                        _state.update { it.copy(isLoading = true) }
                        state.value.customer?.let { customer ->
                            val credit = event.credit.copy(customerId = customer.id)
                            customerService.createCredit(credit)
                        }
                        _state.update { it.copy(isLoading = false) }
                    } catch (e: Exception) {
                        _state.update {
                            it.copy(
                                error = "Error al crear crédito: ${e.message}",
                                isLoading = false
                            )
                        }
                    }
                }
            }

            is CustomerDetailEvent.ProcessPayment -> {
                viewModelScope.launch {
                    try {
                        _state.update { it.copy(isLoading = true) }
                        customerService.processPayment(event.creditId, event.amount)
                        _state.update { it.copy(isLoading = false) }
                    } catch (e: Exception) {
                        _state.update {
                            it.copy(
                                error = "Error al procesar pago: ${e.message}",
                                isLoading = false
                            )
                        }
                    }
                }
            }

            is CustomerDetailEvent.DeleteCustomer -> {
                viewModelScope.launch {
                    try {
                        _state.update { it.copy(isLoading = true) }
                        customerService.deleteCustomer(event.customer)
                        _state.update { it.copy(isLoading = false) }
                    } catch (e: Exception) {
                        _state.update {
                            it.copy(
                                error = "Error al eliminar cliente: ${e.message}",
                                isLoading = false
                            )
                        }
                    }
                }
            }

            CustomerDetailEvent.ClearError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }
} 
