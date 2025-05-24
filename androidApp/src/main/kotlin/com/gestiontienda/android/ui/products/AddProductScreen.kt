package com.gestiontienda.android.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gestiontienda.repository.ProductRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    repository: ProductRepository,
    onProductAdded: () -> Unit,
) {
    var barcode by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf("") }
    var salePrice by remember { mutableStateOf("") }
    var initialStock by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Manual Entry / Scanner Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Product Identification",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("Barcode") },
                        singleLine = true,
                        trailingIcon = {
                            Row {
                                // Search button for manual entry
                                IconButton(
                                    onClick = {
                                        if (barcode.isNotBlank()) {
                                            scope.launch {
                                                isLoading = true
                                                try {
                                                    repository.searchProducts(barcode)
                                                        .onSuccess { products ->
                                                            if (products.isNotEmpty()) {
                                                                val product = products.first()
                                                                name = product.product.product_name
                                                                    ?: ""
                                                            }
                                                        }
                                                } catch (e: Exception) {
                                                    snackbarHostState.showSnackbar(
                                                        "Failed to fetch product info"
                                                    )
                                                } finally {
                                                    isLoading = false
                                                }
                                            }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Search, "Search product")
                                }
                                // Scanner button (placeholder for future implementation)
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                "Barcode scanner will be available in a future update"
                                            )
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.QrCodeScanner, "Scan barcode")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        "Enter barcode manually or use scanner (coming soon)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Product Details Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Product Details",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = purchasePrice,
                            onValueChange = {
                                purchasePrice = it.filter { char ->
                                    char.isDigit() || char == '.'
                                }
                            },
                            label = { Text("Purchase Price") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = salePrice,
                            onValueChange = {
                                salePrice = it.filter { char ->
                                    char.isDigit() || char == '.'
                                }
                            },
                            label = { Text("Sale Price") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = initialStock,
                        onValueChange = {
                            initialStock = it.filter { char ->
                                char.isDigit()
                            }
                        },
                        label = { Text("Initial Stock") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Error message
            errorMessage?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Add button
            Button(
                onClick = {
                    if (validateInput()) {
                        repository.addProductByBarcode(
                            barcode = barcode,
                            purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                            salePrice = salePrice.toDoubleOrNull() ?: 0.0,
                            stock = initialStock.toLongOrNull() ?: 0
                        )
                        onProductAdded()
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Please fill in all required fields correctly"
                            )
                        }
                    }
                },
                modifier = Modifier.align(Alignment.End),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Add Product")
                }
            }
        }
    }
}

@Composable
private fun validateInput(): Boolean {
    return barcode.isNotBlank() &&
            purchasePrice.isNotBlank() &&
            salePrice.isNotBlank() &&
            purchasePrice.toDoubleOrNull() != null &&
            salePrice.toDoubleOrNull() != null &&
            (initialStock.isBlank() || initialStock.toLongOrNull() != null)
} 
