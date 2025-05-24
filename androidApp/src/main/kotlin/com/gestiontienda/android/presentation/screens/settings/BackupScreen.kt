package com.gestiontienda.android.presentation.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onNavigateBack: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val backupFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                val tempFile = File.createTempFile("backup", ".db", context.cacheDir)
                viewModel.onEvent(BackupEvent.CreateLocalBackup(tempFile))
                tempFile.inputStream().use { input ->
                    input.copyTo(outputStream)
                }
                tempFile.delete()
            }
        }
    }

    val restoreFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { inputStream ->
                val tempFile = File.createTempFile("restore", ".db", context.cacheDir)
                tempFile.outputStream().use { output ->
                    inputStream.copyTo(output)
                }
                viewModel.onEvent(BackupEvent.RestoreFromLocal(tempFile))
                tempFile.delete()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Respaldos") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Last backup info
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Último respaldo",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = state.lastBackupDate ?: "No hay respaldos",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Cloud backup options
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Respaldo en la nube",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.onEvent(BackupEvent.BackupToFirebase) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Respaldar")
                        }

                        OutlinedButton(
                            onClick = { viewModel.onEvent(BackupEvent.RestoreFromFirebase) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Restaurar")
                        }
                    }
                }
            }

            // Local backup options
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Respaldo local",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                backupFileLauncher.launch("backup_${System.currentTimeMillis()}.db")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Exportar")
                        }

                        OutlinedButton(
                            onClick = {
                                restoreFileLauncher.launch("application/octet-stream")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Importar")
                        }
                    }
                }
            }

            // Automatic backup settings
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Respaldo automático",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = state.isAutomaticBackupEnabled,
                            onCheckedChange = {
                                viewModel.onEvent(BackupEvent.SetAutomaticBackup(it))
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Habilitar respaldo automático")
                    }

                    if (state.isAutomaticBackupEnabled) {
                        OutlinedTextField(
                            value = state.backupIntervalHours.toString(),
                            onValueChange = { value ->
                                value.toIntOrNull()?.let { hours ->
                                    if (hours > 0) {
                                        viewModel.onEvent(BackupEvent.SetBackupInterval(hours))
                                    }
                                }
                            },
                            label = { Text("Intervalo (horas)") },
                            modifier = Modifier.fillMaxWidth()
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

        state.message?.let { message ->
            Snackbar(
                modifier = Modifier
                    .padding(16.dp),
                action = {
                    TextButton(
                        onClick = { viewModel.onEvent(BackupEvent.DismissMessage) }
                    ) {
                        Text("OK")
                    }
                }
            ) {
                Text(message)
            }
        }

        state.error?.let { error ->
            AlertDialog(
                onDismissRequest = { viewModel.onEvent(BackupEvent.DismissDialog) },
                title = { Text("Error") },
                text = { Text(error) },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.onEvent(BackupEvent.DismissDialog) }
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }
} 
