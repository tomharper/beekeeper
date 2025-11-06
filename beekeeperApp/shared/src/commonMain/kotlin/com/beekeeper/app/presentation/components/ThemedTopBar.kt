// File: fillerApp/shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/components/ThemedTopBar.kt
package com.beekeeper.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeper.app.presentation.theme.ThemeManager

/**
 * Standard themed top bar for all screens
 */
@Composable
fun ThemedTopBar(
    title: String,
    subtitle: String? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit) = {},
    modifier: Modifier = Modifier
) {
    val theme by ThemeManager.currentTheme.collectAsState()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = theme.colors.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Navigation Icon
            navigationIcon?.invoke()

            // Title Section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = if (navigationIcon != null) 8.dp else 16.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.textPrimary
                )
                subtitle?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        color = theme.colors.textSecondary
                    )
                }
            }

            // Actions
            Row {
                actions()
            }
        }
    }
}

/**
 * Simple themed top bar with back navigation
 */
@Composable
fun SimpleThemedTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    actions: @Composable (RowScope.() -> Unit) = {}
) {
    val theme by ThemeManager.currentTheme.collectAsState()

    ThemedTopBar(
        title = title,
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = theme.colors.textPrimary
                )
            }
        },
        actions = actions
    )
}

/**
 * Main app top bar with logo
 */
@Composable
fun MainAppTopBar(
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onNotificationsClick: (() -> Unit)? = null
) {
    val theme by ThemeManager.currentTheme.collectAsState()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = theme.colors.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Logo Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = theme.colors.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "CF",
                            color = theme.colors.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
                Column {
                    Text(
                        "CineFiller",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.colors.textPrimary
                    )
                    Text(
                        "AI Production Platform",
                        fontSize = 12.sp,
                        color = theme.colors.textSecondary
                    )
                }
            }

            // Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                onNotificationsClick?.let {
                    IconButton(onClick = it) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = theme.colors.textPrimary
                        )
                    }
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = theme.colors.textPrimary
                    )
                }
                IconButton(onClick = onProfileClick) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        tint = theme.colors.textPrimary
                    )
                }
            }
        }
    }
}