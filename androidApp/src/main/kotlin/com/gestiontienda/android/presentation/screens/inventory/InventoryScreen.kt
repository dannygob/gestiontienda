package com.gestiontienda.android.presentation.screens.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gestiontienda.android.presentation.components.BarcodeScanner
import com.gestiontienda.android.presentation.components.LoadingOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: InventoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Inventario") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.onEvent(InventoryEvent.ToggleSearchBar) }) {
                            Icon(
                                if (state.isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = if (state.isSearchActive) "Cerrar búsqueda" else "Buscar"
                            )
                        }
                        IconButton(onClick = { viewModel.onEvent(InventoryEvent.ToggleScanner) }) {
                            Icon(
                                Icons.Default.QrCodeScanner,
                                contentDescription = "Escanear código"
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        viewModel.onEvent(
                            InventoryEvent.OnBarcodeDetected("")
                        )
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar producto")
                }
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
                        onQueryChange = { viewModel.onEvent(InventoryEvent.SearchQueryChanged(it)) },
                        onSearch = { viewModel.onEvent(InventoryEvent.Search) },
                        active = state.isSearchActive,
                        onActiveChange = { viewModel.onEvent(InventoryEvent.ToggleSearchBar) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar por nombre o código") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.onEvent(InventoryEvent.ClearSearch) }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                            }
                        }
                    ) {
                        // Search suggestions can be added here later
                    }
                }

                // Category Filter
                if (!state.isSearchActive) {
                    ScrollableTabRow(
                        selectedTabIndex = state.categories.indexOf(state.selectedCategory) + 1,
                        modifier = Modifier.fillMaxWidth(),
                        edgePadding = 16.dp
                    ) {
                        Tab(
                            selected = state.selectedCategory == null,
                            onClick = { viewModel.onEvent(InventoryEvent.SelectCategory(null)) }
                        ) {
                            Text(
                                text = "Todas",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                        state.categories.forEach { category ->
                            Tab(
                                selected = state.selectedCategory == category,
                                onClick = { viewModel.onEvent(InventoryEvent.SelectCategory(category)) }
                            ) {
                                Text(
                                    text = category,
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 12.dp
                                    )
                                )
                            }
                        }
                    }
                }

                if (state.products.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (state.isLoading) "Cargando..." else "No hay productos",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.products) { product ->
                            ProductItem(
                                product = product,
                                onProductClick = {
                                    viewModel.onEvent(
                                        InventoryEvent.SelectProduct(
                                            product
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        if (state.isLoading) {
            LoadingOverlay(message = "Cargando productos...")
        }

        if (state.error != null) {
            Snackbar(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Text(state.error!!)
            }
        }

        if (state.showScanner) {
            BarcodeScanner(
                onBarcodeDetected = { barcode ->
                    viewModel.onEvent(InventoryEvent.OnBarcodeDetected(barcode))
                },
                onClose = {
                    viewModel.onEvent(InventoryEvent.ToggleScanner)
                }
            )
        }

        if (state.showAddDialog && state.selectedProduct != null) {
            ProductDialog(
                product = state.selectedProduct!!,
                categories = state.categories,
                onDismiss = { viewModel.onEvent(InventoryEvent.DismissDialog) },
                onSave = { product ->
                    if (product.id == 0L) {
                        viewModel.onEvent(InventoryEvent.AddProduct(product))
                    } else {
                        viewModel.onEvent(InventoryEvent.UpdateProduct(product))
                    }
                },
                onAddCategory = { category ->
                    viewModel.onEvent(InventoryEvent.AddCategory(category))
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductItem(
    product: ProductEntity,
    onProductClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onProductClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                if (product.barcode != null) {
                    Text(
                        text = "Código: ${product.barcode}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = "Stock: ${product.stock} unidades",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (product.stock <= product.minStock)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "$${product.price}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
} 
