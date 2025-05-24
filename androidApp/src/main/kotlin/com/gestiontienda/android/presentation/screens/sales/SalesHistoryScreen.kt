package com.gestiontienda.android.presentation.screens.sales

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
import com.gestiontienda.android.data.local.entities.SaleStatus
import com.gestiontienda.android.data.local.entities.SaleWithItems
import com.gestiontienda.android.presentation.components.DateRangePicker
import com.gestiontienda.android.presentation.components.LoadingScreen
import com.gestiontienda.android.presentation.theme.spacing
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun SalesHistoryScreen(
    viewModel: SalesHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Ventas") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                LoadingScreen()
            } else {
                SalesHistoryContent(
                    state = state,
                    onEvent = viewModel::onEvent
                )
            }
        }
    }
}

@Composable
private fun SalesHistoryContent(
    state: SalesHistoryState,
    onEvent: (SalesHistoryEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium)
    ) {
        // Date range picker
        DateRangePicker(
            startDate = state.startDate,
            endDate = state.endDate,
            onStartDateSelected = { onEvent(SalesHistoryEvent.SetStartDate(it)) },
            onEndDateSelected = { onEvent(SalesHistoryEvent.SetEndDate(it)) }
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Sales statistics
        SalesStatsCard(state.stats)

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Sales list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            items(state.sales) { sale ->
                SaleCard(
                    sale = sale,
                    onStatusChange = { status ->
                        onEvent(SalesHistoryEvent.UpdateSaleStatus(sale.sale.id, status))
                    }
                )
            }
        }
    }
}

@Composable
private fun SalesStatsCard(stats: SalesStats) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(MaterialTheme.spacing.medium)
                .fillMaxWidth()
        ) {
            Text(
                text = "Resumen de Ventas",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Ventas",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stats.totalSales.toString(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Column {
                    Text(
                        text = "Total Items",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stats.totalItems.toString(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Column {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "$${String.format("%.2f", stats.total)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun SaleCard(
    sale: SaleWithItems,
    onStatusChange: (SaleStatus) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(MaterialTheme.spacing.medium)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = sale.sale.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Total: $${String.format("%.2f", sale.sale.total)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Row {
                    AssistChip(
                        onClick = { showStatusMenu = true },
                        label = { Text(sale.sale.status.name) },
                        leadingIcon = {
                            Icon(
                                imageVector = when (sale.sale.status) {
                                    SaleStatus.COMPLETED -> Icons.Default.CheckCircle
                                    SaleStatus.CANCELLED -> Icons.Default.Cancel
                                    SaleStatus.PENDING -> Icons.Default.Pending
                                    SaleStatus.REFUNDED -> Icons.Default.Reply
                                },
                                contentDescription = null
                            )
                        }
                    )

                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Mostrar menos" else "Mostrar más"
                        )
                    }
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                Divider()
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                Column {
                    Text(
                        text = "Método de pago: ${sale.sale.paymentMethod}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!sale.sale.notes.isNullOrBlank()) {
                        Text(
                            text = "Notas: ${sale.sale.notes}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                    Text(
                        text = "Items:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    sale.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.saleItem.quantity}x ${item.product.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "$${
                                    String.format(
                                        "%.2f",
                                        item.saleItem.priceAtSale * item.saleItem.quantity
                                    )
                                }",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }

    if (showStatusMenu) {
        AlertDialog(
            onDismissRequest = { showStatusMenu = false },
            title = { Text("Cambiar estado") },
            text = {
                Column {
                    SaleStatus.values().forEach { status ->
                        if (status != sale.sale.status) {
                            TextButton(
                                onClick = {
                                    onStatusChange(status)
                                    showStatusMenu = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(status.name)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStatusMenu = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
} 
