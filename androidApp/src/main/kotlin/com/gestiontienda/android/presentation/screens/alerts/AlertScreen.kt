package com.gestiontienda.android.presentation.screens.alerts

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertScreen(
    onNavigateBack: () -> Unit,
    viewModel: AlertViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var showFilterMenu by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alertas de Stock") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtrar")
                    }
                    IconButton(onClick = { viewModel.onEvent(AlertEvent.MarkAllAsRead) }) {
                        Icon(Icons.Default.DoneAll, contentDescription = "Marcar todo como leído")
                    }
                    IconButton(onClick = { viewModel.onEvent(AlertEvent.RefreshAlerts) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
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
            // Filter chips
            FilterSection(
                selectedFilter = state.selectedFilter,
                onFilterSelected = { viewModel.onEvent(AlertEvent.SetFilter(it)) }
            )

            // Show read alerts switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mostrar alertas leídas")
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = state.showReadAlerts,
                    onCheckedChange = { viewModel.onEvent(AlertEvent.ToggleShowRead(it)) }
                )
            }

            if (state.alerts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay alertas",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = state.alerts,
                        key = { it.id }
                    ) { alert ->
                        AlertCard(
                            alert = alert,
                            dateFormat = dateFormat,
                            onMarkAsRead = { viewModel.onEvent(AlertEvent.MarkAsRead(alert.id)) },
                            onDelete = { viewModel.onEvent(AlertEvent.DeleteAlert(alert)) }
                        )
                    }
                }
            }
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        state.error?.let { error ->
            AlertDialog(
                onDismissRequest = { viewModel.onEvent(AlertEvent.DismissError) },
                title = { Text("Error") },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = { viewModel.onEvent(AlertEvent.DismissError) }) {
                        Text("OK")
                    }
                }
            )
        }

        if (showFilterMenu) {
            AlertDialog(
                onDismissRequest = { showFilterMenu = false },
                title = { Text("Filtrar alertas") },
                text = {
                    Column {
                        AlertFilter.values().forEach { filter ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = state.selectedFilter == filter,
                                    onClick = {
                                        viewModel.onEvent(AlertEvent.SetFilter(filter))
                                        showFilterMenu = false
                                    }
                                )
                                Text(
                                    text = when (filter) {
                                        AlertFilter.ALL -> "Todas"
                                        AlertFilter.LOW_STOCK -> "Stock Bajo"
                                        AlertFilter.OUT_OF_STOCK -> "Sin Stock"
                                        AlertFilter.EXPIRING -> "Próximo a Vencer"
                                        AlertFilter.EXPIRED -> "Vencido"
                                    }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showFilterMenu = false }) {
                        Text("Cerrar")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlertCard(
    alert: StockAlert,
    dateFormat: SimpleDateFormat,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (!alert.isRead) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alert.productName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = when (alert.alertType) {
                            AlertType.LOW_STOCK -> "Stock bajo (${alert.currentStock}/${alert.minStock})"
                            AlertType.OUT_OF_STOCK -> "Sin stock"
                            AlertType.EXPIRING_SOON -> "Próximo a vencer"
                            AlertType.EXPIRED -> "Producto vencido"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = dateFormat.format(alert.createdAt),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row {
                    if (!alert.isRead) {
                        IconButton(onClick = onMarkAsRead) {
                            Icon(Icons.Default.Done, contentDescription = "Marcar como leída")
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSection(
    selectedFilter: AlertFilter,
    onFilterSelected: (AlertFilter) -> Unit,
) {
    ScrollableTabRow(
        selectedTabIndex = selectedFilter.ordinal,
        edgePadding = 16.dp
    ) {
        AlertFilter.values().forEachIndexed { index, filter ->
            Tab(
                selected = selectedFilter.ordinal == index,
                onClick = { onFilterSelected(filter) },
                text = {
                    Text(
                        text = when (filter) {
                            AlertFilter.ALL -> "Todas"
                            AlertFilter.LOW_STOCK -> "Stock Bajo"
                            AlertFilter.OUT_OF_STOCK -> "Sin Stock"
                            AlertFilter.EXPIRING -> "Próximo a Vencer"
                            AlertFilter.EXPIRED -> "Vencido"
                        }
                    )
                }
            )
        }
    }
} 
