package com.gestiontienda.android.presentation.screens.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gestiontienda.android.domain.model.StatisticsPeriod
import com.gestiontienda.android.domain.service.ExportFormat
import com.gestiontienda.android.domain.service.ReportFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var showPeriodDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("es", "MX")) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas de Ventas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showPeriodDialog = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleccionar período")
                    }
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Default.Download, contentDescription = "Exportar datos")
                    }
                    IconButton(onClick = { showReportDialog = true }) {
                        Icon(Icons.Default.Description, contentDescription = "Generar reporte")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    PeriodHeader(
                        period = state.selectedPeriod,
                        startDate = state.startDate,
                        endDate = state.endDate,
                        dateFormat = dateFormat
                    )
                }

                state.statistics?.let { stats ->
                    item {
                        SummaryCard(
                            stats = stats,
                            currencyFormat = currencyFormat
                        )
                    }

                    item {
                        ComparisonCard(
                            stats = stats,
                            currencyFormat = currencyFormat
                        )
                    }

                    item {
                        TopProductsCard(
                            products = stats.topProducts,
                            currencyFormat = currencyFormat,
                            onProductClick = { viewModel.onEvent(StatisticsEvent.SelectProduct(it)) }
                        )
                    }

                    item {
                        PaymentMethodsCard(
                            salesByMethod = stats.salesByPaymentMethod,
                            currencyFormat = currencyFormat
                        )
                    }

                    item {
                        SalesByHourCard(
                            salesByHour = stats.salesByHour,
                            currencyFormat = currencyFormat
                        )
                    }
                }
            }
        }

        // Period selection dialog
        if (showPeriodDialog) {
            AlertDialog(
                onDismissRequest = { showPeriodDialog = false },
                title = { Text("Seleccionar Período") },
                text = {
                    Column {
                        StatisticsPeriod.values().forEach { period ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = state.selectedPeriod == period,
                                    onClick = {
                                        viewModel.onEvent(StatisticsEvent.SelectPeriod(period))
                                        showPeriodDialog = false
                                    }
                                )
                                Text(
                                    text = when (period) {
                                        StatisticsPeriod.DAILY -> "Hoy"
                                        StatisticsPeriod.WEEKLY -> "Esta semana"
                                        StatisticsPeriod.MONTHLY -> "Este mes"
                                        StatisticsPeriod.YEARLY -> "Este año"
                                        StatisticsPeriod.CUSTOM -> "Personalizado"
                                    }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPeriodDialog = false }) {
                        Text("Cerrar")
                    }
                }
            )
        }

        // Export dialog
        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = { Text("Exportar Datos") },
                text = {
                    Column {
                        ExportFormat.values().forEach { format ->
                            TextButton(
                                onClick = {
                                    viewModel.onEvent(StatisticsEvent.ExportData(format))
                                    showExportDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = when (format) {
                                        ExportFormat.CSV -> "CSV"
                                        ExportFormat.EXCEL -> "Excel"
                                        ExportFormat.JSON -> "JSON"
                                    }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showExportDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Report dialog
        if (showReportDialog) {
            AlertDialog(
                onDismissRequest = { showReportDialog = false },
                title = { Text("Generar Reporte") },
                text = {
                    Column {
                        ReportFormat.values().forEach { format ->
                            TextButton(
                                onClick = {
                                    viewModel.onEvent(StatisticsEvent.GenerateReport(format))
                                    showReportDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = when (format) {
                                        ReportFormat.PDF -> "PDF"
                                        ReportFormat.EXCEL -> "Excel"
                                    }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showReportDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Error dialog
        state.error?.let { error ->
            AlertDialog(
                onDismissRequest = { viewModel.onEvent(StatisticsEvent.ClearError) },
                title = { Text("Error") },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = { viewModel.onEvent(StatisticsEvent.ClearError) }) {
                        Text("OK")
                    }
                }
            )
        }

        // Export path dialog
        state.exportPath?.let { path ->
            AlertDialog(
                onDismissRequest = { viewModel.onEvent(StatisticsEvent.ClearExportPath) },
                title = { Text("Archivo Generado") },
                text = { Text("El archivo se ha guardado en:\n$path") },
                confirmButton = {
                    TextButton(onClick = { viewModel.onEvent(StatisticsEvent.ClearExportPath) }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
private fun PeriodHeader(
    period: StatisticsPeriod,
    startDate: Date?,
    endDate: Date?,
    dateFormat: SimpleDateFormat,
) {
    Text(
        text = when (period) {
            StatisticsPeriod.DAILY -> "Estadísticas de Hoy"
            StatisticsPeriod.WEEKLY -> "Estadísticas de la Semana"
            StatisticsPeriod.MONTHLY -> "Estadísticas del Mes"
            StatisticsPeriod.YEARLY -> "Estadísticas del Año"
            StatisticsPeriod.CUSTOM -> {
                if (startDate != null && endDate != null) {
                    "Del ${dateFormat.format(startDate)} al ${dateFormat.format(endDate)}"
                } else {
                    "Período Personalizado"
                }
            }
        },
        style = MaterialTheme.typography.titleLarge
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SummaryCard(
    stats: com.gestiontienda.android.domain.model.SalesStatistics,
    currencyFormat: NumberFormat,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Resumen",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            StatRow(
                label = "Ventas Totales",
                value = currencyFormat.format(stats.totalSales)
            )
            StatRow(
                label = "Productos Vendidos",
                value = stats.totalItems.toString()
            )
            StatRow(
                label = "Ticket Promedio",
                value = currencyFormat.format(stats.averageTicket)
            )
            StatRow(
                label = "Margen de Ganancia",
                value = "${String.format("%.1f", stats.profitMargin)}%"
            )
            StatRow(
                label = "Ganancia Total",
                value = currencyFormat.format(stats.totalProfit)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComparisonCard(
    stats: com.gestiontienda.android.domain.model.SalesStatistics,
    currencyFormat: NumberFormat,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Comparación con Período Anterior",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            GrowthRow(
                label = "Ventas",
                growth = stats.comparisonWithPreviousPeriod.salesGrowth
            )
            GrowthRow(
                label = "Productos",
                growth = stats.comparisonWithPreviousPeriod.itemsGrowth
            )
            GrowthRow(
                label = "Ganancia",
                growth = stats.comparisonWithPreviousPeriod.profitGrowth
            )
            GrowthRow(
                label = "Ticket Promedio",
                growth = stats.comparisonWithPreviousPeriod.averageTicketGrowth
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopProductsCard(
    products: List<com.gestiontienda.android.domain.model.ProductSalesStats>,
    currencyFormat: NumberFormat,
    onProductClick: (Long) -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Productos Más Vendidos",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            products.forEach { product ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.productName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${product.quantitySold} unidades · ${
                                currencyFormat.format(
                                    product.totalRevenue
                                )
                            }",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    IconButton(onClick = { onProductClick(product.productId) }) {
                        Icon(Icons.Default.Info, contentDescription = "Ver detalles")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentMethodsCard(
    salesByMethod: Map<String, Double>,
    currencyFormat: NumberFormat,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Ventas por Método de Pago",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            salesByMethod.forEach { (method, amount) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(method)
                    Text(currencyFormat.format(amount))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SalesByHourCard(
    salesByHour: Map<Int, Double>,
    currencyFormat: NumberFormat,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Ventas por Hora",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            salesByHour.forEach { (hour, amount) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${hour}:00")
                    Text(currencyFormat.format(amount))
                }
            }
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(
            text = value,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun GrowthRow(
    label: String,
    growth: Double,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(
            text = "${String.format("%.1f", growth)}%",
            color = when {
                growth > 0 -> Color.Green
                growth < 0 -> Color.Red
                else -> Color.Unspecified
            },
            fontWeight = FontWeight.Bold
        )
    }
} 
