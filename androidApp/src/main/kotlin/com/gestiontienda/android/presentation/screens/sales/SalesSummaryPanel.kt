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
import com.gestiontienda.android.data.local.dao.SaleWithSummary
import com.gestiontienda.android.data.local.dao.SalesSummary
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesSummaryPanel(
    summary: SalesSummary?,
    recentSales: List<SaleWithSummary>,
    selectedRange: DateRange,
    onRangeSelected: (DateRange) -> Unit,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        // Date range selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Resumen de Ventas",
                style = MaterialTheme.typography.titleLarge
            )

            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = {}
            ) {
                OutlinedTextField(
                    value = when (selectedRange) {
                        DateRange.TODAY -> "Hoy"
                        DateRange.LAST_WEEK -> "Última Semana"
                        DateRange.LAST_MONTH -> "Último Mes"
                        DateRange.CUSTOM -> "Personalizado"
                    },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = false)
                    },
                    modifier = Modifier.menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = false,
                    onDismissRequest = {}
                ) {
                    DropdownMenuItem(
                        text = { Text("Hoy") },
                        onClick = { onRangeSelected(DateRange.TODAY) }
                    )
                    DropdownMenuItem(
                        text = { Text("Última Semana") },
                        onClick = { onRangeSelected(DateRange.LAST_WEEK) }
                    )
                    DropdownMenuItem(
                        text = { Text("Último Mes") },
                        onClick = { onRangeSelected(DateRange.LAST_MONTH) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary cards
        if (summary != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryCard(
                    title = "Total Ventas",
                    value = currencyFormatter.format(summary.totalSales),
                    icon = Icons.Default.AttachMoney,
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    title = "# Ventas",
                    value = summary.saleCount.toString(),
                    icon = Icons.Default.Receipt,
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    title = "# Productos",
                    value = summary.totalItems.toString(),
                    icon = Icons.Default.Inventory,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent sales
        Text(
            text = "Ventas Recientes",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (recentSales.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay ventas recientes",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recentSales) { sale ->
                    SaleCard(
                        sale = sale,
                        currencyFormatter = currencyFormatter
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaleCard(
    sale: SaleWithSummary,
    currencyFormatter: NumberFormat,
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "MX")) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = dateFormatter.format(Date(sale.date)),
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (sale.customerName != null) {
                        Text(
                            text = sale.customerName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Text(
                    text = currencyFormatter.format(sale.total),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${sale.totalQuantity} productos",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (sale.paymentMethod) {
                            "EFECTIVO" -> Icons.Default.Money
                            "TARJETA" -> Icons.Default.CreditCard
                            else -> Icons.Default.Payment
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = sale.paymentMethod,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                AssistChip(
                    onClick = { },
                    label = { Text(sale.status) },
                    leadingIcon = {
                        Icon(
                            imageVector = when (sale.status) {
                                "COMPLETED" -> Icons.Default.CheckCircle
                                "CANCELLED" -> Icons.Default.Cancel
                                else -> Icons.Default.Pending
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            if (sale.notes != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = sale.notes,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
} 
