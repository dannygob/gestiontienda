package com.gestiontienda.android.presentation.screens.customers

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
import com.gestiontienda.android.domain.model.Customer
import com.gestiontienda.android.domain.model.CustomerCredit
import com.gestiontienda.android.domain.model.CustomerStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customerId: Long,
    onNavigateBack: () -> Unit,
    viewModel: CustomerDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showCreditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("es", "MX")) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    LaunchedEffect(customerId) {
        viewModel.loadCustomer(customerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Cliente") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.customer?.status == CustomerStatus.ACTIVE) {
                FloatingActionButton(onClick = { showCreditDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo crédito")
                }
            }
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
            state.customer?.let { customer ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Customer info card
                    item {
                        CustomerInfoCard(
                            customer = customer,
                            currencyFormat = currencyFormat,
                            dateFormat = dateFormat
                        )
                    }

                    // Statistics card
                    item {
                        state.statistics?.let { stats ->
                            CustomerStatsCard(
                                stats = stats,
                                currencyFormat = currencyFormat
                            )
                        }
                    }

                    // Credits section
                    item {
                        Text(
                            text = "Créditos",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    items(state.credits) { credit ->
                        CreditCard(
                            credit = credit,
                            currencyFormat = currencyFormat,
                            dateFormat = dateFormat,
                            onPayment = { amount ->
                                viewModel.onEvent(
                                    CustomerDetailEvent.ProcessPayment(
                                        credit.id,
                                        amount
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }

        // Edit dialog
        if (showEditDialog) {
            state.customer?.let { customer ->
                EditCustomerDialog(
                    customer = customer,
                    onDismiss = { showEditDialog = false },
                    onCustomerUpdated = { updatedCustomer ->
                        viewModel.onEvent(CustomerDetailEvent.UpdateCustomer(updatedCustomer))
                        showEditDialog = false
                    }
                )
            }
        }

        // New credit dialog
        if (showCreditDialog) {
            AddCreditDialog(
                onDismiss = { showCreditDialog = false },
                onCreditAdded = { credit ->
                    viewModel.onEvent(CustomerDetailEvent.AddCredit(credit))
                    showCreditDialog = false
                }
            )
        }

        // Delete confirmation dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Eliminar Cliente") },
                text = { Text("¿Estás seguro que deseas eliminar este cliente? Esta acción no se puede deshacer.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            state.customer?.let {
                                viewModel.onEvent(CustomerDetailEvent.DeleteCustomer(it))
                            }
                            showDeleteDialog = false
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Error dialog
        state.error?.let { error ->
            AlertDialog(
                onDismissRequest = { viewModel.onEvent(CustomerDetailEvent.ClearError) },
                title = { Text("Error") },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = { viewModel.onEvent(CustomerDetailEvent.ClearError) }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerInfoCard(
    customer: Customer,
    currencyFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
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
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.titleLarge
                )
                CustomerStatusChip(status = customer.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            customer.email?.let {
                InfoRow(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = it
                )
            }

            customer.phone?.let {
                InfoRow(
                    icon = Icons.Default.Phone,
                    label = "Teléfono",
                    value = it
                )
            }

            customer.address?.let {
                InfoRow(
                    icon = Icons.Default.LocationOn,
                    label = "Dirección",
                    value = it
                )
            }

            customer.taxId?.let {
                InfoRow(
                    icon = Icons.Default.Badge,
                    label = "RFC",
                    value = it
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Crédito disponible",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = currencyFormat.format(customer.creditLimit - customer.currentCredit),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Puntos",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = customer.loyaltyPoints.toString(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerStatsCard(
    stats: com.gestiontienda.android.domain.service.CustomerStatistics,
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
                text = "Estadísticas",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            StatRow(
                label = "Total de compras",
                value = currencyFormat.format(stats.totalPurchases)
            )
            StatRow(
                label = "Número de compras",
                value = stats.purchaseCount.toString()
            )
            StatRow(
                label = "Compra promedio",
                value = currencyFormat.format(stats.averagePurchase)
            )
            StatRow(
                label = "Crédito utilizado",
                value = currencyFormat.format(stats.totalCreditUsed)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreditCard(
    credit: CustomerCredit,
    currencyFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    onPayment: (Double) -> Unit,
) {
    var showPaymentDialog by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
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
                Column {
                    Text(
                        text = currencyFormat.format(credit.amount),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Vence: ${dateFormat.format(credit.dueDate)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (credit.status == com.gestiontienda.android.domain.model.CreditStatus.ACTIVE) {
                    FilledTonalButton(onClick = { showPaymentDialog = true }) {
                        Text("Pagar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = (credit.amount - credit.remainingAmount) / credit.amount,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Restante: ${currencyFormat.format(credit.remainingAmount)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = credit.status.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (credit.status) {
                        com.gestiontienda.android.domain.model.CreditStatus.ACTIVE -> MaterialTheme.colorScheme.primary
                        com.gestiontienda.android.domain.model.CreditStatus.PAID -> MaterialTheme.colorScheme.secondary
                        com.gestiontienda.android.domain.model.CreditStatus.OVERDUE -> MaterialTheme.colorScheme.error
                        com.gestiontienda.android.domain.model.CreditStatus.CANCELLED -> MaterialTheme.colorScheme.error
                    }
                )
            }
        }
    }

    if (showPaymentDialog) {
        PaymentDialog(
            maxAmount = credit.remainingAmount,
            currencyFormat = currencyFormat,
            onDismiss = { showPaymentDialog = false },
            onConfirm = { amount ->
                onPayment(amount)
                showPaymentDialog = false
            }
        )
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
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
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentDialog(
    maxAmount: Double,
    currencyFormat: NumberFormat,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
) {
    var amount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Realizar Pago") },
        text = {
            Column {
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        error = try {
                            val value = it.toDoubleOrNull() ?: 0.0
                            when {
                                value <= 0.0 -> "El monto debe ser mayor a cero"
                                value > maxAmount -> "El monto no puede ser mayor al saldo pendiente"
                                else -> null
                            }
                        } catch (e: Exception) {
                            "Monto inválido"
                        }
                    },
                    label = { Text("Monto") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Saldo pendiente: ${currencyFormat.format(maxAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    amount.toDoubleOrNull()?.let { value ->
                        if (value > 0 && value <= maxAmount) {
                            onConfirm(value)
                        }
                    }
                },
                enabled = error == null && amount.isNotBlank()
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditCustomerDialog(
    customer: Customer,
    onDismiss: () -> Unit,
    onCustomerUpdated: (Customer) -> Unit,
) {
    var name by remember { mutableStateOf(customer.name) }
    var email by remember { mutableStateOf(customer.email ?: "") }
    var phone by remember { mutableStateOf(customer.phone ?: "") }
    var address by remember { mutableStateOf(customer.address ?: "") }
    var taxId by remember { mutableStateOf(customer.taxId ?: "") }
    var creditLimit by remember { mutableStateOf(customer.creditLimit.toString()) }
    var status by remember { mutableStateOf(customer.status) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Cliente") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = taxId,
                    onValueChange = { taxId = it },
                    label = { Text("RFC") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = creditLimit,
                    onValueChange = { creditLimit = it },
                    label = { Text("Límite de crédito") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Estado",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CustomerStatus.values().forEach { customerStatus ->
                        FilterChip(
                            selected = status == customerStatus,
                            onClick = { status = customerStatus },
                            label = {
                                Text(
                                    when (customerStatus) {
                                        CustomerStatus.ACTIVE -> "Activo"
                                        CustomerStatus.INACTIVE -> "Inactivo"
                                        CustomerStatus.BLOCKED -> "Bloqueado"
                                    }
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onCustomerUpdated(
                            customer.copy(
                                name = name.trim(),
                                email = email.takeIf { it.isNotBlank() }?.trim(),
                                phone = phone.takeIf { it.isNotBlank() }?.trim(),
                                address = address.takeIf { it.isNotBlank() }?.trim(),
                                taxId = taxId.takeIf { it.isNotBlank() }?.trim(),
                                creditLimit = creditLimit.toDoubleOrNull() ?: customer.creditLimit,
                                status = status
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCreditDialog(
    onDismiss: () -> Unit,
    onCreditAdded: (CustomerCredit) -> Unit,
) {
    var amount by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(Date()) }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Crédito") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        error = try {
                            val value = it.toDoubleOrNull() ?: 0.0
                            if (value <= 0.0) "El monto debe ser mayor a cero" else null
                        } catch (e: Exception) {
                            "Monto inválido"
                        }
                    },
                    label = { Text("Monto") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                // TODO: Add date picker
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    amount.toDoubleOrNull()?.let { value ->
                        if (value > 0) {
                            onCreditAdded(
                                CustomerCredit(
                                    customerId = 0, // Will be set by the ViewModel
                                    amount = value,
                                    remainingAmount = value,
                                    dueDate = dueDate,
                                    status = com.gestiontienda.android.domain.model.CreditStatus.ACTIVE,
                                    notes = notes.takeIf { it.isNotBlank() }
                                )
                            )
                        }
                    }
                },
                enabled = error == null && amount.isNotBlank()
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
} 
