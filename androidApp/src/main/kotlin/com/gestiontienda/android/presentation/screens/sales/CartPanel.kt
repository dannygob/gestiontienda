package com.gestiontienda.android.presentation.screens.sales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartPanel(
    items: List<SaleItem>,
    stockErrors: List<StockValidationError>,
    onUpdateQuantity: (product: ProductEntity, quantity: Int) -> Unit,
    onUpdateDiscount: (product: ProductEntity, discount: Double) -> Unit,
    onRemoveItem: (product: ProductEntity) -> Unit,
    onClearCart: () -> Unit,
    onCompleteSale: (paymentMethod: String, customerName: String?, notes: String?) -> Unit,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier,
) {
    var showCompleteSaleDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            text = "Carrito de Venta",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "El carrito está vacío",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
                    val stockError = stockErrors.find { it.productName == item.product.name }
                    CartItemCard(
                        item = item,
                        stockError = stockError,
                        onUpdateQuantity = { onUpdateQuantity(item.product, it) },
                        onUpdateDiscount = { onUpdateDiscount(item.product, it) },
                        onRemove = { onRemoveItem(item.product) },
                        currencyFormatter = currencyFormatter
                    )
                }
            }
        }

        // Cart summary and actions
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            // Show stock errors summary if any
            if (stockErrors.isNotEmpty()) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Stock Insuficiente",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        stockErrors.forEach { error ->
                            Text(
                                text = "• ${error.productName}: Solicitado ${error.requested}, Disponible ${error.available}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Total
            val total = items.sumOf { item ->
                (item.priceAtSale * (1 - item.discount)) * item.quantity
            }

            Text(
                text = "Total: ${currencyFormatter.format(total)}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onClearCart,
                    enabled = items.isNotEmpty()
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Limpiar")
                }

                Button(
                    onClick = { showCompleteSaleDialog = true },
                    enabled = items.isNotEmpty() && stockErrors.isEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Completar Venta")
                }
            }
        }
    }

    if (showCompleteSaleDialog) {
        CompleteSaleDialog(
            onDismiss = { showCompleteSaleDialog = false },
            onConfirm = { paymentMethod, customerName, notes ->
                onCompleteSale(paymentMethod, customerName, notes)
                showCompleteSaleDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartItemCard(
    item: SaleItem,
    stockError: StockValidationError?,
    onUpdateQuantity: (Int) -> Unit,
    onUpdateDiscount: (Double) -> Unit,
    onRemove: () -> Unit,
    currencyFormatter: NumberFormat,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (stockError != null) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.product.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Precio: ${currencyFormatter.format(item.priceAtSale)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (stockError != null) {
                        Text(
                            text = "Stock disponible: ${stockError.available}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = item.quantity.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { if (it > 0) onUpdateQuantity(it) }
                    },
                    label = { Text("Cantidad") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    isError = stockError != null
                )

                OutlinedTextField(
                    value = (item.discount * 100).toInt().toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let {
                            if (it in 0..100) onUpdateDiscount(it / 100.0)
                        }
                    },
                    label = { Text("Descuento %") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            val subtotal = (item.priceAtSale * (1 - item.discount)) * item.quantity
            Text(
                text = "Subtotal: ${currencyFormatter.format(subtotal)}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun CompleteSaleDialog(
    onDismiss: () -> Unit,
    onConfirm: (paymentMethod: String, customerName: String?, notes: String?) -> Unit,
) {
    var paymentMethod by remember { mutableStateOf("EFECTIVO") }
    var customerName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Completar Venta") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = {}
                ) {
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = { paymentMethod = it },
                        label = { Text("Método de Pago") },
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = false)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = false,
                        onDismissRequest = {}
                    ) {
                        listOf("EFECTIVO", "TARJETA", "TRANSFERENCIA").forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) },
                                onClick = { paymentMethod = method }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Nombre del Cliente (Opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas (Opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        paymentMethod,
                        customerName.takeIf { it.isNotBlank() },
                        notes.takeIf { it.isNotBlank() }
                    )
                }
            ) {
                Text("Completar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
} 
