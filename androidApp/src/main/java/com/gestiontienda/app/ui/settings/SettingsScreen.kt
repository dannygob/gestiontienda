package com.gestiontienda.android.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gestiontienda.android.R
import com.gestiontienda.android.utils.LocaleUtils

@Composable
fun SettingsScreen(
    onNavigateToLanguageSelection: () -> Unit,
    onNavigateToStoreSettings: () -> Unit,
    onNavigateToBackupSettings: () -> Unit,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        SmallTopAppBar(
            title = { Text(text = stringResource(R.string.nav_settings)) }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Store Settings
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToStoreSettings() }
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null
                        )
                        Text(text = stringResource(R.string.store_settings))
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null
                    )
                }
            }

            // Language Selection
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToLanguageSelection() }
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null
                        )
                        Column {
                            Text(text = stringResource(R.string.language))
                            val currentLanguage = LocaleUtils.getAvailableLanguages()
                                .find { it.code == LocaleUtils.getStoredLanguage(context) }
                            Text(
                                text = currentLanguage?.nativeName ?: "English",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null
                    )
                }
            }

            // Backup Settings
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToBackupSettings() }
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Backup,
                            contentDescription = null
                        )
                        Text(text = stringResource(R.string.backup_settings))
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null
                    )
                }
            }
        }
    }
} 
