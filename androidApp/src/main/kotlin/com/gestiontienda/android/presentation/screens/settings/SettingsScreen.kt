package com.gestiontienda.android.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gestiontienda.android.data.local.entities.Permission
import com.gestiontienda.android.data.local.entities.UserRole
import com.gestiontienda.android.domain.model.StoreConfig
import com.gestiontienda.android.domain.model.UserWithRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToLanguageSelection: () -> Unit, // Added this line
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var showStoreConfigDialog by remember { mutableStateOf(false) }
    var showUserManagementDialog by remember { mutableStateOf(false) }
    var showAddUserDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Store Configuration
            ListItem(
                headlineContent = { Text("Configuración de la Tienda") },
                supportingContent = { Text("Información general, impuestos y recibos") },
                leadingContent = {
                    Icon(Icons.Default.Store, contentDescription = null)
                },
                modifier = Modifier.clickable { showStoreConfigDialog = true }
            )

            // User Management
            if (state.hasManageUsersPermission) {
                ListItem(
                    headlineContent = { Text("Gestión de Usuarios") },
                    supportingContent = { Text("Roles y permisos") },
                    leadingContent = {
                        Icon(Icons.Default.People, contentDescription = null)
                    },
                    modifier = Modifier.clickable { showUserManagementDialog = true }
                )
            }

            // Language Selection
            ListItem(
                headlineContent = { Text("Idioma") }, // TODO: Use string resources
                supportingContent = { Text("Cambiar el idioma de la aplicación") }, // TODO: Use string resources
                leadingContent = {
                    Icon(Icons.Default.Language, contentDescription = null)
                },
                modifier = Modifier.clickable { onNavigateToLanguageSelection() }
            )

            // Backup option
            ListItem(
                headlineContent = { Text("Respaldos") },
                supportingContent = { Text("Gestionar respaldos y restauración") },
                leadingContent = {
                    Icon(Icons.Default.Backup, contentDescription = null)
                },
                modifier = Modifier.clickable { onNavigateToBackup() }
            )
        }

        // Store Configuration Dialog
        if (showStoreConfigDialog) {
            state.storeConfig?.let { config ->
                StoreConfigDialog(
                    config = config,
                    onDismiss = { showStoreConfigDialog = false },
                    onSave = { updatedConfig ->
                        viewModel.onEvent(SettingsEvent.UpdateStoreConfig(updatedConfig))
                        showStoreConfigDialog = false
                    }
                )
            }
        }

        // User Management Dialog
        if (showUserManagementDialog) {
            UserManagementDialog(
                users = state.users,
                onDismiss = { showUserManagementDialog = false },
                onAddUser = { showAddUserDialog = true },
                onUpdateUserRole = { userId, role ->
                    viewModel.onEvent(SettingsEvent.UpdateUserRole(userId, role))
                },
                onDeleteUser = { userId ->
                    viewModel.onEvent(SettingsEvent.DeleteUser(userId))
                }
            )
        }

        // Add User Dialog
        if (showAddUserDialog) {
            AddUserDialog(
                onDismiss = { showAddUserDialog = false },
                onAddUser = { email, name, role ->
                    viewModel.onEvent(SettingsEvent.AddUser(email, name, role))
                    showAddUserDialog = false
                }
            )
        }

        // Error Dialog
        state.error?.let { error ->
            AlertDialog(
                onDismissRequest = { viewModel.onEvent(SettingsEvent.ClearError) },
                title = { Text("Error") },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = { viewModel.onEvent(SettingsEvent.ClearError) }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoreConfigDialog(
    config: StoreConfig,
    onDismiss: () -> Unit,
    onSave: (StoreConfig) -> Unit,
) {
    var storeName by remember { mutableStateOf(config.storeName) }
    var storeAddress by remember { mutableStateOf(config.storeAddress ?: "") }
    var storePhone by remember { mutableStateOf(config.storePhone ?: "") }
    var storeEmail by remember { mutableStateOf(config.storeEmail ?: "") }
    var taxRate by remember { mutableStateOf(config.taxRate.toString()) }
    var receiptHeader by remember { mutableStateOf(config.receiptHeader ?: "") }
    var receiptFooter by remember { mutableStateOf(config.receiptFooter ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configuración de la Tienda") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text("Nombre de la Tienda") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = storeAddress,
                    onValueChange = { storeAddress = it },
                    label = { Text("Dirección") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = storePhone,
                    onValueChange = { storePhone = it },
                    label = { Text("Teléfono") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = storeEmail,
                    onValueChange = { storeEmail = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = taxRate,
                    onValueChange = { taxRate = it },
                    label = { Text("Tasa de IVA (%)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = receiptHeader,
                    onValueChange = { receiptHeader = it },
                    label = { Text("Encabezado del Recibo") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = receiptFooter,
                    onValueChange = { receiptFooter = it },
                    label = { Text("Pie del Recibo") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        config.copy(
                            storeName = storeName,
                            storeAddress = storeAddress.takeIf { it.isNotBlank() },
                            storePhone = storePhone.takeIf { it.isNotBlank() },
                            storeEmail = storeEmail.takeIf { it.isNotBlank() },
                            taxRate = taxRate.toDoubleOrNull() ?: config.taxRate,
                            receiptHeader = receiptHeader.takeIf { it.isNotBlank() },
                            receiptFooter = receiptFooter.takeIf { it.isNotBlank() }
                        )
                    )
                }
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
private fun UserManagementDialog(
    users: List<UserWithRole>,
    onDismiss: () -> Unit,
    onAddUser: () -> Unit,
    onUpdateUserRole: (String, UserRole) -> Unit,
    onDeleteUser: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gestión de Usuarios") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onAddUser,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar Usuario")
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn {
                    items(users) { user ->
                        UserItem(
                            user = user,
                            onUpdateRole = { role -> onUpdateUserRole(user.id, role) },
                            onDelete = { onDeleteUser(user.id) }
                        )
                        Divider()
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
private fun UserItem(
    user: UserWithRole,
    onUpdateRole: (UserRole) -> Unit,
    onDelete: () -> Unit,
) {
    var showRoleDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(user.name) },
        supportingContent = { Text(user.email) },
        trailingContent = {
            Row {
                IconButton(onClick = { showRoleDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Cambiar rol")
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    )

    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = { Text("Cambiar Rol") },
            text = {
                Column {
                    UserRole.values().forEach { role ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdateRole(role)
                                    showRoleDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = user.role == role,
                                onClick = {
                                    onUpdateRole(role)
                                    showRoleDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(role.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoleDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Usuario") },
            text = { Text("¿Estás seguro que deseas eliminar este usuario?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddUserDialog(
    onDismiss: () -> Unit,
    onAddUser: (String, String, UserRole) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.CASHIER) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Usuario") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Rol",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                UserRole.values().forEach { role ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedRole = role }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(role.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (email.isNotBlank() && name.isNotBlank()) {
                        onAddUser(email, name, selectedRole)
                    }
                },
                enabled = email.isNotBlank() && name.isNotBlank()
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
} 
