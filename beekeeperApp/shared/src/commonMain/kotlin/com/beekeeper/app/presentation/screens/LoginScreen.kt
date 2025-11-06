// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/LoginScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.beekeeper.app.presentation.viewmodels.AuthViewModel
import com.beekeeper.app.presentation.theme.rememberAppTheme

/**
 * Login screen for user authentication
 * Matches React web app's LoginScreen functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val theme = rememberAppTheme()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    // Listen for successful authentication
    LaunchedEffect(Unit) {
        authViewModel.authState.collect { state ->
            if (state is com.beekeeper.app.domain.auth.AuthState.Authenticated) {
                onLoginSuccess()
            }
        }
    }

    // Full-screen layout matching React app
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Header with CineFiller branding
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            color = theme.colors.surface,
            shadowElevation = 1.dp
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CineFiller",
                        style = MaterialTheme.typography.headlineMedium,
                        color = theme.colors.primary
                    )
                }
                // Purple accent border
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = theme.colors.primary.copy(alpha = 0.1f)
                    ) {}
                }
            }
        }

        // Centered content area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                colors = CardDefaults.cardColors(
                    containerColor = theme.colors.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // "Team Login" heading
                    Text(
                        text = "Team Login",
                        style = MaterialTheme.typography.headlineMedium,
                        color = theme.colors.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Error alert
                    if (errorMessage != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = theme.colors.errorContainer
                            )
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = theme.colors.onErrorContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    // Username field
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.colors.onSurface,
                            unfocusedTextColor = theme.colors.onSurface,
                            focusedBorderColor = theme.colors.primary,
                            unfocusedBorderColor = theme.colors.outline
                        )
                    )

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        singleLine = true,
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (username.isNotBlank() && password.isNotBlank()) {
                                    authViewModel.login(username, password)
                                }
                            }
                        ),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) {
                                        Icons.Default.Visibility
                                    } else {
                                        Icons.Default.VisibilityOff
                                    },
                                    contentDescription = if (passwordVisible) {
                                        "Hide password"
                                    } else {
                                        "Show password"
                                    },
                                    tint = theme.colors.onSurfaceVariant
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.colors.onSurface,
                            unfocusedTextColor = theme.colors.onSurface,
                            focusedBorderColor = theme.colors.primary,
                            unfocusedBorderColor = theme.colors.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Login button
                    Button(
                        onClick = {
                            if (username.isNotBlank() && password.isNotBlank()) {
                                authViewModel.login(username, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !isLoading && username.isNotBlank() && password.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.colors.primary,
                            contentColor = theme.colors.onPrimary,
                            disabledContainerColor = theme.colors.surfaceVariant
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = theme.colors.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Sign In")
                        }
                    }

                    // Footer text
                    Text(
                        text = "Team credentials required",
                        style = MaterialTheme.typography.bodyMedium,
                        color = theme.colors.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    // Development helper button
                    TextButton(
                        onClick = {
                            username = "cinefiller-team"
                            password = "cinefiller-team-2024"
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = "Use Default Credentials",
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.colors.primary
                        )
                    }
                }
            }
        }
    }
}
