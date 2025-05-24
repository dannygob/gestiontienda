package com.gestiontienda.android.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.gestiontienda.android.data.local.entities.ProductEntity
import com.gestiontienda.android.presentation.theme.spacing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter

@Composable
fun ProductSearchBar(
    onProductSelected: (ProductEntity) -> Unit,
    viewModel: ProductSearchViewModel = hiltViewModel(),
) {
    var showDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    OutlinedButton(
        onClick = { showDialog = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Buscar producto",
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text("Buscar producto")
    }

    if (showDialog) {
        Dialog(
            onDismissRequest = {
                showDialog = false
                viewModel.clearSearch()
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                shape = MaterialTheme.shapes.large,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(MaterialTheme.spacing.medium)
                ) {
                    OutlinedTextField(
                        value = viewModel.searchQuery,
                        onValueChange = viewModel::onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = { Text("Buscar por nombre o código de barras") },
                        singleLine = true,
                        trailingIcon = {
                            if (viewModel.searchQuery.isNotEmpty()) {
                                IconButton(onClick = viewModel::clearSearch) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Limpiar búsqueda"
                                    )
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                    LazyColumn {
                        items(viewModel.searchResults) { product ->
                            ProductSearchItem(
                                product = product,
                                onClick = {
                                    onProductSelected(product)
                                    showDialog = false
                                    viewModel.clearSearch()
                                    focusManager.clearFocus()
                                }
                            )
                        }
                    }

                    if (viewModel.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

@Composable
private fun ProductSearchItem(
    product: ProductEntity,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        tonalElevation = 0.dp
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
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Stock: ${product.stock} | Precio: $${
                        String.format(
                            "%.2f",
                            product.salePrice
                        )
                    }",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (product.stock <= product.minStock) {
                AssistChip(
                    onClick = null,
                    label = { Text("Stock bajo") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        labelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
            }
        }
    }
}

@HiltViewModel
class ProductSearchViewModel @Inject constructor(
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    var searchQuery by mutableStateOf("")
        private set

    private val _isLoading = MutableStateFlow(false)
    var isLoading by mutableStateOf(false)
        private set

    var searchResults by mutableStateOf<List<ProductEntity>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .filter { it.length >= 2 }
                .collect { query ->
                    _isLoading.value = true
                    isLoading = true
                    try {
                        searchResults = productRepository.searchProducts(query)
                    } finally {
                        _isLoading.value = false
                        isLoading = false
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery = query
        _searchQuery.value = query
    }

    fun clearSearch() {
        searchQuery = ""
        _searchQuery.value = ""
        searchResults = emptyList()
    }
} 
