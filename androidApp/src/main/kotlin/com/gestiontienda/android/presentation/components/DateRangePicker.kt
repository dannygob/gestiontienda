package com.gestiontienda.android.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.gestiontienda.android.presentation.theme.spacing
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun DateRangePicker(
    startDate: LocalDateTime,
    endDate: LocalDateTime,
    onStartDateSelected: (LocalDateTime) -> Unit,
    onEndDateSelected: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = { showStartDatePicker = true },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(startDate.format(dateFormatter))
        }

        Text("hasta")

        OutlinedButton(
            onClick = { showEndDatePicker = true },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(endDate.format(dateFormatter))
        }
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            onDateSelected = {
                onStartDateSelected(it)
                showStartDatePicker = false
            },
            initialDate = startDate
        )
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            onDateSelected = {
                onEndDateSelected(it)
                showEndDatePicker = false
            },
            initialDate = endDate
        )
    }
}

@Composable
private fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (LocalDateTime) -> Unit,
    initialDate: LocalDateTime,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Seleccionar fecha") },
        text = {
            // Here you would typically use a date picker widget
            // For now, we'll use a simple text that shows we need to implement this
            Text("TODO: Implement Material3 DatePicker when available for Compose")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // For now, we'll just pass the initial date back
                    onDateSelected(initialDate)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
} 
