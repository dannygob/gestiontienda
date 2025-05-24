package com.gestiontienda.android.presentation.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.gestiontienda.android.presentation.components.LoadingScreen
import com.gestiontienda.android.presentation.theme.spacing

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state.isEmailSent) {
        if (state.isEmailSent) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar Contraseña") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingScreen(message = "Enviando correo...")
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(MaterialTheme.spacing.medium),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Ingresa tu correo electrónico para recuperar tu contraseña",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                OutlinedTextField(
                    value = state.email,
                    onValueChange = { viewModel.onEvent(ForgotPasswordEvent.EmailChanged(it)) },
                    label = { Text("Correo electrónico") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.onEvent(ForgotPasswordEvent.SendResetEmail)
                        }
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null)
                    },
                    isError = state.emailError != null,
                    supportingText = state.emailError?.let { { Text(it) } }
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                Button(
                    onClick = { viewModel.onEvent(ForgotPasswordEvent.SendResetEmail) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                ) {
                    Text("Enviar Correo")
                }

                state.error?.let { error ->
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (state.isEmailSent) {
                    AlertDialog(
                        onDismissRequest = onNavigateBack,
                        title = { Text("Correo enviado") },
                        text = { Text("Se ha enviado un correo con las instrucciones para restablecer tu contraseña.") },
                        confirmButton = {
                            TextButton(onClick = onNavigateBack) {
                                Text("OK")
                            }
                        }
                    )
                }
            }
        }
    }
} 
