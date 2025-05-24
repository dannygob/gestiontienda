package com.gestiontienda.android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gestiontienda.android.navigation.ActionType
import com.gestiontienda.android.navigation.NavigationEvent
import com.gestiontienda.android.util.ToastUtil

@Composable
fun SampleScreen() {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Primary button with toast
            Button(
                onClick = {
                    ToastUtil.handleNavigationEvent(
                        context,
                        NavigationEvent.Action(
                            description = "Primary Action",
                            type = ActionType.CLICK
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Primary Button")
            }

            // Secondary button with toast
            OutlinedButton(
                onClick = {
                    ToastUtil.handleNavigationEvent(
                        context,
                        NavigationEvent.Action(
                            description = "Secondary Action",
                            type = ActionType.CLICK
                        )
                    )
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Secondary Button")
            }

            // Text button with long press
            TextButton(
                onClick = {
                    ToastUtil.handleNavigationEvent(
                        context,
                        NavigationEvent.Action(
                            description = "Text Button Action",
                            type = ActionType.CLICK
                        )
                    )
                },
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            ToastUtil.handleNavigationEvent(
                                context,
                                NavigationEvent.Action(
                                    description = "Text Button",
                                    type = ActionType.LONG_PRESS
                                )
                            )
                        }
                    )
                }
            ) {
                Text("Press or Long Press")
            }

            // Card with clickable surface
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                onClick = {
                    ToastUtil.handleNavigationEvent(
                        context,
                        NavigationEvent.Action(
                            description = "Interactive Card",
                            type = ActionType.CLICK
                        )
                    )
                }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Clickable Card",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Selection example
            var selected by remember { mutableStateOf(false) }
            Switch(
                checked = selected,
                onCheckedChange = { newValue ->
                    selected = newValue
                    ToastUtil.handleNavigationEvent(
                        context,
                        NavigationEvent.Action(
                            description = "Switch ${if (newValue) "enabled" else "disabled"}",
                            type = ActionType.SELECTION
                        )
                    )
                }
            )

            // Input example
            OutlinedTextField(
                value = "",
                onValueChange = { newValue ->
                    if (newValue.isNotEmpty()) {
                        ToastUtil.handleNavigationEvent(
                            context,
                            NavigationEvent.Action(
                                description = "Text input received",
                                type = ActionType.INPUT
                            )
                        )
                    }
                },
                label = { Text("Input Example") }
            )

            // Confirmation/Cancellation example
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        ToastUtil.handleNavigationEvent(
                            context,
                            NavigationEvent.Action(
                                description = "Action confirmed",
                                type = ActionType.CONFIRMATION
                            )
                        )
                    }
                ) {
                    Text("Confirm")
                }

                OutlinedButton(
                    onClick = {
                        ToastUtil.handleNavigationEvent(
                            context,
                            NavigationEvent.Action(
                                description = "Action cancelled",
                                type = ActionType.CANCELLATION
                            )
                        )
                    }
                ) {
                    Text("Cancel")
                }
            }
        }
    }
} 