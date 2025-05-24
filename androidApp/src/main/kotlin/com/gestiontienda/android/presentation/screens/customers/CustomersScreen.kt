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
import com.gestiontienda.android.domain.model.CustomerStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCustomerDetail: (Long) -> Unit,
    viewModel: CustomersViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("es", "MX")) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clientes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtrar")
                    }
                    IconButton(onClick = { showAddCustomerDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar cliente")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddCustomerDialog = true }) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Agregar cliente")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onEvent(CustomersEvent.SearchQueryChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar clientes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            // Status filter chips
            ScrollableTabRow(
                selectedTabIndex = CustomerStatus.values().indexOf(state.selectedStatus),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                CustomerStatus.values().forEach { status ->
                    Tab(
                        selected = state.selectedStatus == status,
                        onClick = { viewModel.onEvent(CustomersEvent.StatusFilterChanged(status)) },
                        text = {
                            Text(
                                when (status) {
                                    CustomerStatus.ACTIVE -> "Activos"
                                    CustomerStatus.INACTIVE -> "Inactivos"
                                    CustomerStatus.BLOCKED -> "Bloqueados"
                                }
                            )
                        }
                    )
                }
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.customers) { customer ->
                        CustomerCard(
                            customer = customer,
                            currencyFormat = currencyFormat,
                            dateFormat = dateFormat,
                            onClick = { onNavigateToCustomerDetail(customer.id) }
                        )
                    }
                }
            }
        }

        // Add customer dialog
        if (showAddCustomerDialog) {
            AddCustomerDialog(
                onDismiss = { showAddCustomerDialog = false },
                onCustomerAdded = { customer ->
                    viewModel.onEvent(CustomersEvent.AddCustomer(customer))
                    showAddCustomerDialog = false
                }
            )
        }

        // Filter dialog
        if (showFilterDialog) {
            FilterDialog(
                currentStatus = state.selectedStatus,
                onStatusSelected = { status ->
                    viewModel.onEvent(CustomersEvent.StatusFilterChanged(status))
                    showFilterDialog = false
                },
                onDismiss = { showFilterDialog = false }
            )
        }

        // Error dialog
        state.error?.let { error ->
            AlertDialog(
                onDismissRequest = { viewModel.onEvent(CustomersEvent.ClearError) },
                title = { Text("Error") },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = { viewModel.onEvent(CustomersEvent.ClearError) }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerCard(
    customer: Customer,
    currencyFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    customer.email?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                CustomerStatusChip(status = customer.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Compras totales",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = currencyFormat.format(customer.totalPurchases),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Puntos",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = customer.loyaltyPoints.toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (customer.currentCredit > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Crédito actual",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = currencyFormat.format(customer.currentCredit),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerStatusChip(status: CustomerStatus) {
    val (color, text) = when (status) {
        CustomerStatus.ACTIVE -> MaterialTheme.colorScheme.primary to "Activo"
        CustomerStatus.INACTIVE -> MaterialTheme.colorScheme.secondary to "Inactivo"
        CustomerStatus.BLOCKED -> MaterialTheme.colorScheme.error to "Bloqueado"
    }

    SuggestionChip(
        onClick = { },
        label = { Text(text) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = color.copy(alpha = 0.1f),
            labelColor = color
        )
    )
}

@Composable
private fun FilterDialog(
    currentStatus: CustomerStatus,
    onStatusSelected: (CustomerStatus) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtrar por estado") },
        text = {
            Column {
                CustomerStatus.values().forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentStatus == status,
                            onClick = { onStatusSelected(status) }
                        )
                        Text(
                            text = when (status) {
                                CustomerStatus.ACTIVE -> "Activos"
                                CustomerStatus.INACTIVE -> "Inactivos"
                                CustomerStatus.BLOCKED -> "Bloqueados"
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onCustomerAdded: (Customer) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var taxId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Cliente") },
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
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onCustomerAdded(
                            Customer(
                                name = name.trim(),
                                email = email.takeIf { it.isNotBlank() }?.trim(),
                                phone = phone.takeIf { it.isNotBlank() }?.trim(),
                                address = address.takeIf { it.isNotBlank() }?.trim(),
                                taxId = taxId.takeIf { it.isNotBlank() }?.trim()
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
