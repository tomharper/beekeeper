// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/AISettingsScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeper.app.domain.model.BedrockConfigPrefs
import com.beekeeper.app.domain.model.UserPreferences

@Composable
fun AISettingsScreen(
    preferences: UserPreferences,
    onPreferencesUpdate: (UserPreferences) -> Unit,
    onNavigateBack: () -> Unit
) {
    var currentPreferences by remember { mutableStateOf(preferences) }
    var showApiKey by remember { mutableStateOf(false) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AISettingsTopBar(
                onNavigateBack = {
                    if (hasUnsavedChanges) {
                        // Show confirmation dialog
                        onNavigateBack()
                    } else {
                        onNavigateBack()
                    }
                },
                onSave = {
                    onPreferencesUpdate(currentPreferences)
                    hasUnsavedChanges = false
                },
                hasUnsavedChanges = hasUnsavedChanges
            )
        },
        containerColor = Color(0xFF1A1A1A)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Script Generation Services
            item {
                ServiceCategoryHeader(
                    title = "Script Generation Services",
                    icon = Icons.Default.Edit
                )
            }

            // Claude Direct API
            item {
                ApiKeySection(
                    title = "Claude Direct API",
                    description = "Direct access to Anthropic's Claude models",
                    apiKey = currentPreferences.claudeApiKey ?: "",
                    showApiKey = showApiKey,
                    onApiKeyChange = { key ->
                        currentPreferences = currentPreferences.copy(claudeApiKey = key.ifEmpty { null })
                        hasUnsavedChanges = true
                    },
                    onToggleVisibility = { showApiKey = !showApiKey },
                    isConfigured = !currentPreferences.claudeApiKey.isNullOrEmpty()
                )
            }

            // AWS Bedrock
            item {
                BedrockConfigSection(
                    config = currentPreferences.bedrockConfig,
                    onConfigChange = { config ->
                        currentPreferences = currentPreferences.copy(bedrockConfig = config)
                        hasUnsavedChanges = true
                    }
                )
            }

            // Avatar & Video Generation Services
            item {
                ServiceCategoryHeader(
                    title = "Avatar & Video Generation Services",
                    icon = Icons.Default.VideoLibrary
                )
            }

            // HeyGen
            item {
                ApiKeySection(
                    title = "HeyGen",
                    description = "AI avatar and video generation",
                    apiKey = currentPreferences.heygenApiKey ?: "",
                    showApiKey = showApiKey,
                    onApiKeyChange = { key ->
                        currentPreferences = currentPreferences.copy(heygenApiKey = key.ifEmpty { null })
                        hasUnsavedChanges = true
                    },
                    onToggleVisibility = { showApiKey = !showApiKey },
                    isConfigured = !currentPreferences.heygenApiKey.isNullOrEmpty()
                )
            }

            // D-ID
            item {
                ApiKeySection(
                    title = "D-ID",
                    description = "Digital human video platform",
                    apiKey = currentPreferences.didApiKey ?: "",
                    showApiKey = showApiKey,
                    onApiKeyChange = { key ->
                        currentPreferences = currentPreferences.copy(didApiKey = key.ifEmpty { null })
                        hasUnsavedChanges = true
                    },
                    onToggleVisibility = { showApiKey = !showApiKey },
                    isConfigured = !currentPreferences.didApiKey.isNullOrEmpty()
                )
            }

            // Synthesia
            item {
                ApiKeySection(
                    title = "Synthesia",
                    description = "AI video generation platform",
                    apiKey = currentPreferences.synthesiaApiKey ?: "",
                    showApiKey = showApiKey,
                    onApiKeyChange = { key ->
                        currentPreferences = currentPreferences.copy(synthesiaApiKey = key.ifEmpty { null })
                        hasUnsavedChanges = true
                    },
                    onToggleVisibility = { showApiKey = !showApiKey },
                    isConfigured = !currentPreferences.synthesiaApiKey.isNullOrEmpty()
                )
            }

            // Instructions
            item {
                InstructionsCard()
            }
        }
    }
}

@Composable
fun AISettingsTopBar(
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    hasUnsavedChanges: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Text(
            "AI Service Settings",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        TextButton(
            onClick = onSave,
            enabled = hasUnsavedChanges
        ) {
            Text(
                "Save",
                color = if (hasUnsavedChanges) Color(0xFF4A90E2) else Color.Gray
            )
        }
    }
}

@Composable
fun ServiceCategoryHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = Color(0xFF4A90E2),
            modifier = Modifier.size(24.dp)
        )
        Text(
            title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ApiKeySection(
    title: String,
    description: String,
    apiKey: String,
    showApiKey: Boolean,
    onApiKeyChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    isConfigured: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        description,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }

                if (isConfigured) {
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                "Configured",
                                color = Color(0xFF4CAF50),
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Configured",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF4CAF50)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f),
                            labelColor = Color(0xFF4CAF50),
                            leadingIconContentColor = Color(0xFF4CAF50)
                        )
                    )
                }
            }

            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChange,
                label = { Text("API Key") },
                placeholder = { Text("Enter your API key") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = onToggleVisibility) {
                        Icon(
                            if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showApiKey) "Hide" else "Show",
                            tint = Color.Gray
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4A90E2),
                    unfocusedBorderColor = Color(0xFF3A3A3A),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )
        }
    }
}

@Composable
fun BedrockConfigSection(
    config: BedrockConfigPrefs?,
    onConfigChange: (BedrockConfigPrefs?) -> Unit
) {
    var expanded by remember { mutableStateOf(config != null) }
    var region by remember { mutableStateOf(config?.region ?: "us-east-1") }
    var accessKeyId by remember { mutableStateOf(config?.accessKeyId ?: "") }
    var secretAccessKey by remember { mutableStateOf(config?.secretAccessKey ?: "") }
    var showSecretKey by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "AWS Bedrock",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Access Claude via AWS Bedrock",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }

                Switch(
                    checked = expanded,
                    onCheckedChange = {
                        expanded = it
                        if (!it) {
                            onConfigChange(null)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4A90E2)
                    )
                )
            }

            if (expanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = region,
                        onValueChange = {
                            region = it
                            if (accessKeyId.isNotEmpty() && secretAccessKey.isNotEmpty()) {
                                onConfigChange(BedrockConfigPrefs(region, accessKeyId, secretAccessKey))
                            }
                        },
                        label = { Text("AWS Region") },
                        placeholder = { Text("e.g., us-east-1") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4A90E2),
                            unfocusedBorderColor = Color(0xFF3A3A3A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = accessKeyId,
                        onValueChange = {
                            accessKeyId = it
                            if (it.isNotEmpty() && secretAccessKey.isNotEmpty() && region.isNotEmpty()) {
                                onConfigChange(BedrockConfigPrefs(region, accessKeyId, secretAccessKey))
                            }
                        },
                        label = { Text("Access Key ID") },
                        placeholder = { Text("Enter AWS Access Key ID") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4A90E2),
                            unfocusedBorderColor = Color(0xFF3A3A3A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = secretAccessKey,
                        onValueChange = {
                            secretAccessKey = it
                            if (accessKeyId.isNotEmpty() && it.isNotEmpty() && region.isNotEmpty()) {
                                onConfigChange(BedrockConfigPrefs(region, accessKeyId, secretAccessKey))
                            }
                        },
                        label = { Text("Secret Access Key") },
                        placeholder = { Text("Enter AWS Secret Access Key") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showSecretKey) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { showSecretKey = !showSecretKey }) {
                                Icon(
                                    if (showSecretKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showSecretKey) "Hide" else "Show",
                                    tint = Color.Gray
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4A90E2),
                            unfocusedBorderColor = Color(0xFF3A3A3A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun InstructionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A3A5A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Info",
                    tint = Color(0xFF4A90E2),
                    modifier = Modifier.size(24.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Getting Started",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        "1. Sign up for the AI services you want to use",
                        color = Color(0xFFB0B0B0),
                        fontSize = 14.sp
                    )

                    Text(
                        "2. Get your API keys from their respective dashboards",
                        color = Color(0xFFB0B0B0),
                        fontSize = 14.sp
                    )

                    Text(
                        "3. Enter the API keys above and save",
                        color = Color(0xFFB0B0B0),
                        fontSize = 14.sp
                    )

                    Text(
                        "4. You can switch between providers in the video editor",
                        color = Color(0xFFB0B0B0),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}