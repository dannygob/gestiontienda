package com.gestiontienda.android.presentation.screens.sales

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gestiontienda.android.data.local.entities.PaymentMethod
import com.gestiontienda.android.presentation.components.LoadingScreen
import com.gestiontienda.android.presentation.components.ProductSearchBar
import com.gestiontienda.android.presentation.theme.spacing
import com.gestiontienda.android.presentation.components.LoadingOverlay
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    onNavigateBack: () -> Unit,
    viewModel: SalesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val locale = Locale("es", "MX")
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(locale) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Ventas") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.onEvent(SalesEvent.ToggleSearchBar) }) {
                            Icon(
                                if (state.isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = if (state.isSearchActive) "Cerrar búsqueda" else "Buscar"
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (state.isSearchActive) {
                    SearchBar(
                        query = state.searchQuery,
                        onQueryChange = { viewModel.onEvent(SalesEvent.SearchQueryChanged(it)) },
                        onSearch = { /* Handle search */ },
                        active = state.isSearchActive,
                        onActiveChange = { viewModel.onEvent(SalesEvent.ToggleSearchBar) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar productos") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            IconButton(
                                onClick = { viewModel.onEvent(SalesEvent.SearchQueryChanged("")) }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                            }
                        }
                    ) {
                        // Search results will be shown here
                        if (state.selectedProduct != null) {
                            ListItem(
                                headlineContent = { Text(state.selectedProduct.name) },
                                supportingContent = {
                                    Text(
                                        "Stock: ${state.selectedProduct.stock} - Precio: ${
                                            currencyFormatter.format(
                                                state.selectedProduct.price
                                            )
                                        }"
                                    )
                                },
                                leadingContent = {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                                },
                                modifier = Modifier.clickable {
                                    viewModel.onEvent(SalesEvent.SelectProduct(state.selectedProduct))
                                }
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxSize()) {
                    // Left panel: Cart
                    CartPanel(
                        items = state.currentSaleItems,
                        stockErrors = state.stockErrors,
                        onUpdateQuantity = { product, quantity ->
                            viewModel.onEvent(SalesEvent.UpdateQuantity(product, quantity))
                        },
                        onUpdateDiscount = { product, discount ->
                            viewModel.onEvent(SalesEvent.UpdateDiscount(product, discount))
                        },
                        onRemoveItem = { product ->
                            viewModel.onEvent(SalesEvent.RemoveProduct(product))
                        },
                        onClearCart = {
                            viewModel.onEvent(SalesEvent.ClearCart)
                        },
                        onCompleteSale = { paymentMethod, customerName, notes ->
                            viewModel.onEvent(
                                SalesEvent.CompleteSale(
                                    paymentMethod = paymentMethod,
                                    customerName = customerName,
                                    notes = notes
                                )
                            )
                        },
                        currencyFormatter = currencyFormatter,
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxHeight()
                    )

                    Divider(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                    )

                    // Right panel: Sales summary and history
                    SalesSummaryPanel(
                        summary = state.salesSummary,
                        recentSales = state.recentSales,
                        selectedRange = state.dateRange,
                        onRangeSelected = { range ->
                            viewModel.onEvent(SalesEvent.SetDateRange(range))
                        },
                        currencyFormatter = currencyFormatter,
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxHeight()
                    )
                }
            }
        }

        if (state.isLoading) {
            LoadingOverlay()
        }

        state.error?.let { error ->
            Snackbar(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                action = {
                    TextButton(
                        onClick = { viewModel.onEvent(SalesEvent.DismissDialog) }
                    ) {
                        Text("OK")
                    }
                }
            ) {
                Text(error)
            }
        }
    }
}

@Composable
private fun SalesContent(
    state: SalesState,
    onEvent: (SalesEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium)
    ) {
        // Product search
        ProductSearchBar(
            onProductSelected = { product ->
                onEvent(SalesEvent.AddToCart(product))
            }
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Cart items
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(state.cart) { cartItem ->
                CartItemCard(
                    cartItem = cartItem,
                    onQuantityChange = { quantity ->
                        onEvent(SalesEvent.UpdateQuantity(cartItem.product, quantity))
                    },
                    onRemove = {
                        onEvent(SalesEvent.RemoveFromCart(cartItem.product))
                    }
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            }
        }

        // Payment section
        PaymentSection(
            total = state.total,
            selectedPaymentMethod = state.paymentMethod,
            notes = state.notes,
            onPaymentMethodSelected = { method ->
                onEvent(SalesEvent.SetPaymentMethod(method))
            },
            onNotesChanged = { notes ->
                onEvent(SalesEvent.SetNotes(notes))
            }
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = { onEvent(SalesEvent.ClearCart) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Limpiar")
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

            Button(
                onClick = { onEvent(SalesEvent.ProcessSale) },
                modifier = Modifier.weight(1f),
                enabled = state.cart.isNotEmpty()
            ) {
                Text("Procesar Venta")
            }
        }
    }
}

@Composable
private fun CartItemCard(
    cartItem: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(MaterialTheme.spacing.medium)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cartItem.product.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Precio: $${cartItem.product.salePrice}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                IconButton(
                    onClick = { onQuantityChange(cartItem.quantity - 1) }
                ) {
                    Icon(Icons.Default.Remove, "Reducir cantidad")
                }

                Text(
                    text = cartItem.quantity.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.widthIn(min = 24.dp),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = { onQuantityChange(cartItem.quantity + 1) }
                ) {
                    Icon(Icons.Default.Add, "Aumentar cantidad")
                }

                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, "Eliminar")
                }
            }
        }
    }
}

@Composable
private fun PaymentSection(
    total: Double,
    selectedPaymentMethod: PaymentMethod,
    notes: String,
    onPaymentMethodSelected: (PaymentMethod) -> Unit,
    onNotesChanged: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(MaterialTheme.spacing.medium)
                .fillMaxWidth()
        ) {
            Text(
                text = "Total: $${String.format("%.2f", total)}",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            Text(
                text = "Método de pago",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                PaymentMethod.values().forEach { method ->
                    FilterChip(
                        selected = selectedPaymentMethod == method,
                        onClick = { onPaymentMethodSelected(method) },
                        label = { Text(method.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChanged,
                label = { Text("Notas") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }
    }
}

@Composable
private fun ErrorSnackbar(
    message: String,
    onDismiss: () -> Unit,
) {
    Snackbar(
        modifier = Modifier.padding(MaterialTheme.spacing.medium),
        action = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    ) {
        Text(message)
    }
} 
