package com.gestiontienda.android.presentation.screens.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.gestiontienda.android.data.local.entities.ProductEntity
import com.gestiontienda.android.presentation.components.CategorySelector

@Composable
fun ProductDialog(
    product: ProductEntity,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit,
    onAddCategory: (String) -> Unit,
) {
    var name by remember { mutableStateOf(product.name) }
    var category by remember { mutableStateOf(product.category) }
    var price by remember { mutableStateOf(product.price.toString()) }
    var stock by remember { mutableStateOf(product.stock.toString()) }
    var minStock by remember { mutableStateOf(product.minStock.toString()) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var stockError by remember { mutableStateOf<String?>(null) }
    var minStockError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (product.id == 0L) "Agregar Producto" else "Editar Producto",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = if (it.isBlank()) "El nombre es requerido" else null
                    },
                    label = { Text("Nombre") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                CategorySelector(
                    selectedCategory = category,
                    categories = categories,
                    onCategorySelected = { category = it },
                    onAddCategory = onAddCategory,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        price = it
                        priceError = try {
                            if (it.toDouble() < 0) "El precio no puede ser negativo" else null
                        } catch (e: NumberFormatException) {
                            "Precio inválido"
                        }
                    },
                    label = { Text("Precio") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = priceError != null,
                    supportingText = priceError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = stock,
                        onValueChange = {
                            stock = it
                            stockError = try {
                                if (it.toInt() < 0) "El stock no puede ser negativo" else null
                            } catch (e: NumberFormatException) {
                                "Stock inválido"
                            }
                        },
                        label = { Text("Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = stockError != null,
                        supportingText = stockError?.let { { Text(it) } },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = minStock,
                        onValueChange = {
                            minStock = it
                            minStockError = try {
                                if (it.toInt() < 0) "El stock mínimo no puede ser negativo" else null
                            } catch (e: NumberFormatException) {
                                "Stock mínimo inválido"
                            }
                        },
                        label = { Text("Stock Mínimo") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = minStockError != null,
                        supportingText = minStockError?.let { { Text(it) } },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            // Validate fields
                            nameError = if (name.isBlank()) "El nombre es requerido" else null
                            priceError = try {
                                if (price.toDouble() < 0) "El precio no puede ser negativo" else null
                            } catch (e: NumberFormatException) {
                                "Precio inválido"
                            }
                            stockError = try {
                                if (stock.toInt() < 0) "El stock no puede ser negativo" else null
                            } catch (e: NumberFormatException) {
                                "Stock inválido"
                            }
                            minStockError = try {
                                if (minStock.toInt() < 0) "El stock mínimo no puede ser negativo" else null
                            } catch (e: NumberFormatException) {
                                "Stock mínimo inválido"
                            }

                            if (nameError == null && priceError == null &&
                                stockError == null && minStockError == null
                            ) {
                                onSave(
                                    product.copy(
                                        name = name,
                                        category = category,
                                        price = price.toDoubleOrNull() ?: 0.0,
                                        stock = stock.toIntOrNull() ?: 0,
                                        minStock = minStock.toIntOrNull() ?: 5
                                    )
                                )
                            }
                        }
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
} 
