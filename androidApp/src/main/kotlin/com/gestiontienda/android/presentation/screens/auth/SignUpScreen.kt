package com.gestiontienda.android.presentation.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.gestiontienda.android.presentation.components.LoadingScreen
import com.gestiontienda.android.presentation.theme.spacing

@Composable
fun SignUpScreen(
    onNavigateBack: () -> Unit,
    onSignUpSuccess: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state.isSignedUp) {
        if (state.isSignedUp) {
            onSignUpSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Cuenta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingScreen(message = "Creando cuenta...")
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
                    text = "Completa tus datos",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                NameField(
                    name = state.name,
                    onNameChange = { viewModel.onEvent(SignUpEvent.NameChanged(it)) },
                    error = state.nameError,
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                EmailField(
                    email = state.email,
                    onEmailChange = { viewModel.onEvent(SignUpEvent.EmailChanged(it)) },
                    error = state.emailError,
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                PasswordField(
                    password = state.password,
                    onPasswordChange = { viewModel.onEvent(SignUpEvent.PasswordChanged(it)) },
                    error = state.passwordError,
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                ConfirmPasswordField(
                    confirmPassword = state.confirmPassword,
                    onConfirmPasswordChange = {
                        viewModel.onEvent(
                            SignUpEvent.ConfirmPasswordChanged(
                                it
                            )
                        )
                    },
                    error = state.confirmPasswordError,
                    onDone = { viewModel.onEvent(SignUpEvent.SignUp) }
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                Button(
                    onClick = { viewModel.onEvent(SignUpEvent.SignUp) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                ) {
                    Text("Crear Cuenta")
                }

                state.error?.let { error ->
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun NameField(
    name: String,
    onNameChange: (String) -> Unit,
    error: String?,
    onNext: () -> Unit,
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Nombre") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { onNext() }
        ),
        leadingIcon = {
            Icon(Icons.Default.Person, contentDescription = null)
        },
        isError = error != null,
        supportingText = error?.let { { Text(it) } }
    )
}

@Composable
private fun ConfirmPasswordField(
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    error: String?,
    onDone: () -> Unit,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = confirmPassword,
        onValueChange = onConfirmPasswordChange,
        label = { Text("Confirmar contraseña") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onDone() }
        ),
        leadingIcon = {
            Icon(Icons.Default.Lock, contentDescription = null)
        },
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                )
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        isError = error != null,
        supportingText = error?.let { { Text(it) } }
    )
} 
