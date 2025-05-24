package com.gestiontienda.android.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gestiontienda.android.R
import com.gestiontienda.android.utils.LocaleUtils
import com.gestiontienda.android.utils.Language

@Composable
fun LanguageSelectionScreen(
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    var selectedLanguage by remember { mutableStateOf(LocaleUtils.getStoredLanguage(context)) }
    val languages = LocaleUtils.getAvailableLanguages()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        SmallTopAppBar(
            title = { Text(text = stringResource(R.string.language_selection)) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(languages) { language ->
                LanguageItem(
                    language = language,
                    isSelected = selectedLanguage == language.code,
                    onSelect = { code ->
                        selectedLanguage = code
                        LocaleUtils.setLocale(context, code)
                        onNavigateBack()
                    }
                )
            }
        }
    }
}

@Composable
private fun LanguageItem(
    language: Language,
    isSelected: Boolean,
    onSelect: (String) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(language.code) }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = language.nativeName,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = language.englishName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
} 
