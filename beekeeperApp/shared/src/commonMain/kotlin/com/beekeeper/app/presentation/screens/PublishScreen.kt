// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/PublishScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeper.app.presentation.components.SecondaryTopBar
import com.beekeeper.app.presentation.theme.ThemeManager

/**
 * Publish Screen - Distribution and platform publishing
 * Matches React PublishScreen.tsx
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishScreen(
    projectId: String,
    onNavigateBack: () -> Unit
) {
    val theme by ThemeManager.currentTheme.collectAsState()

    Scaffold(
        topBar = {
            SecondaryTopBar(
                title = "Publish",
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = theme.colors.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Export Formats Section
            item {
                Text(
                    text = "Export Formats",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.textPrimary
                )
            }

            item {
                ExportFormatCard(
                    name = "MP4 4K",
                    description = "High quality 4K video (3840x2160)",
                    fileSize = "2.4 GB",
                    onExport = { /* TODO */ },
                    theme = theme
                )
            }

            item {
                ExportFormatCard(
                    name = "MP4 1080p",
                    description = "Full HD video (1920x1080)",
                    fileSize = "850 MB",
                    onExport = { /* TODO */ },
                    theme = theme
                )
            }

            item {
                ExportFormatCard(
                    name = "WebM",
                    description = "Web-optimized format",
                    fileSize = "620 MB",
                    onExport = { /* TODO */ },
                    theme = theme
                )
            }

            // Platform Publishing Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Publishing Platforms",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.textPrimary
                )
            }

            item {
                PlatformCard(
                    name = "YouTube",
                    icon = Icons.Default.SmartDisplay,
                    isConnected = false,
                    onConnect = { /* TODO */ },
                    theme = theme
                )
            }

            item {
                PlatformCard(
                    name = "Facebook",
                    icon = Icons.Default.Facebook,
                    isConnected = false,
                    onConnect = { /* TODO */ },
                    theme = theme
                )
            }

            item {
                PlatformCard(
                    name = "Twitter",
                    icon = Icons.Default.Public,
                    isConnected = false,
                    onConnect = { /* TODO */ },
                    theme = theme
                )
            }

            // Bottom padding for navigation bar
            item {
                Spacer(modifier = Modifier.height(88.dp))
            }
        }
    }
}

@Composable
private fun ExportFormatCard(
    name: String,
    description: String,
    fileSize: String,
    onExport: () -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = theme.colors.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = theme.colors.textPrimary
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = theme.colors.textSecondary
                )
                Text(
                    text = fileSize,
                    fontSize = 12.sp,
                    color = theme.colors.textSecondary
                )
            }
            Button(
                onClick = onExport,
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.colors.primary
                )
            ) {
                Icon(Icons.Default.Download, contentDescription = "Export")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export")
            }
        }
    }
}

@Composable
private fun PlatformCard(
    name: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isConnected: Boolean,
    onConnect: () -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = theme.colors.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    icon,
                    contentDescription = name,
                    tint = theme.colors.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.colors.textPrimary
                    )
                    Text(
                        text = if (isConnected) "Connected" else "Not connected",
                        fontSize = 14.sp,
                        color = if (isConnected) theme.colors.success else theme.colors.textSecondary
                    )
                }
            }
            Button(
                onClick = onConnect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isConnected) theme.colors.surface else theme.colors.primary
                ),
                border = if (isConnected) androidx.compose.foundation.BorderStroke(
                    1.dp,
                    theme.colors.outline
                ) else null
            ) {
                Text(if (isConnected) "Manage" else "Connect")
            }
        }
    }
}
