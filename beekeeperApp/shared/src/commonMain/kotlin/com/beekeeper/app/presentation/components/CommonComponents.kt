// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/components/CommonComponents.kt
package com.beekeeper.app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeper.app.GlobalNavItem
import com.beekeeper.app.ProjectNavItem
import com.beekeeper.app.presentation.theme.rememberAppTheme

/**
 * Global Navigation Bar - displays for Homepage, Projects, Avatar Creation, Tutorials, and Profile
 */
@Composable
fun GlobalNavigationBar(
    selectedItem: GlobalNavItem,
    onItemSelected: (GlobalNavItem) -> Unit
) {
    val theme = rememberAppTheme()

    NavigationBar(
        containerColor = theme.colors.surface,
        contentColor = theme.colors.onSurface,
        tonalElevation = 0.dp
    ) {
        GlobalNavItem.values().forEach { item ->
            NavigationBarItem(
                selected = selectedItem == item,
                onClick = { onItemSelected(item) },
                icon = {
                    Icon(
                        if (selectedItem == item) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title,
                        tint = if (selectedItem == item) theme.colors.primary else theme.colors.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        item.title,
                        color = if (selectedItem == item) theme.colors.primary else theme.colors.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = theme.colors.primaryContainer
                )
            )
        }
    }
}

/**
 * Project-Specific Navigation Bar - displays when user is within a project context
 */
@Composable
fun ProjectNavigationBar(
    selectedItem: ProjectNavItem,
    onItemSelected: (ProjectNavItem) -> Unit,
    projectId: String
) {
    val theme = rememberAppTheme()

    NavigationBar(
        containerColor = theme.colors.surface,
        contentColor = theme.colors.onSurface,
        tonalElevation = 0.dp
    ) {
        ProjectNavItem.values().forEach { item ->
            NavigationBarItem(
                selected = selectedItem == item,
                onClick = { onItemSelected(item) },
                icon = {
                    Icon(
                        if (selectedItem == item) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title,
                        tint = if (selectedItem == item) theme.colors.primary else theme.colors.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        item.title,
                        color = if (selectedItem == item) theme.colors.primary else theme.colors.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = theme.colors.primaryContainer
                )
            )
        }
    }
}


/**
 * Primary Top Bar for main navigation screens (no back arrow)
 * Used for: Home, Projects, Avatar Creation, Tutorials, Profile,
 * and project screens like Stories Hub, Character Hub, etc.
 */
@Composable
fun PrimaryTopBar(
    title: String,
    subtitle: String? = null,
    showThemeToggle: Boolean = true,
    showNotifications: Boolean = true,
    unreadNotificationCount: Int = 0,
    onNotificationClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    val theme = rememberAppTheme()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = theme.colors.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    color = theme.colors.onSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                subtitle?.let {
                    Text(
                        text = it,
                        color = theme.colors.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Custom actions first
                actions()

                // Notifications button
                if (showNotifications) {
                    Box {
                        IconButton(onClick = onNotificationClick) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Notifications",
                                tint = theme.colors.onSurface
                            )
                        }

                        // Unread badge
                        if (unreadNotificationCount > 0) {
                            Box(
                                modifier = Modifier
                                    .offset(x = 24.dp, y = 8.dp)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(theme.colors.error),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (unreadNotificationCount > 9) "9+" else unreadNotificationCount.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Theme toggle button
                if (showThemeToggle) {
                    val currentTheme = rememberAppTheme() // Re-observe theme to get updates
                    IconButton(
                        onClick = {
                            // Use platform-specific toggle (handles user preference on iOS)
                            com.beekeeper.app.presentation.theme.toggleThemeWithUserPreference()
                        }
                    ) {
                        Icon(
                            imageVector = if (currentTheme.isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = if (currentTheme.isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                            tint = theme.colors.onSurface
                        )
                    }
                }
            }
        }
    }
}

/**
 * Enhanced Secondary Top Bar with rich features
 * Features:
 * - Back navigation
 * - Breadcrumb support
 * - Quick actions menu
 * - Search capability
 * - Status indicators
 * - AI Assistant toggle
 * - Export/Share options
 *
 * Can be used simply like the old SecondaryTopBar:
 * SecondaryTopBar(title = "Title", onNavigateBack = { })
 *
 * Or with full features for complex screens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondaryTopBar(
    title: String,
    subtitle: String? = null,
    breadcrumbs: List<Breadcrumb> = emptyList(),
    onNavigateBack: () -> Unit,
    searchConfig: SearchConfig? = null,
    statusIndicator: StatusIndicator? = null,
    aiAssistantEnabled: Boolean = false,
    onAiAssistantToggle: ((Boolean) -> Unit)? = null,
    quickActions: List<QuickAction> = emptyList(),
    exportOptions: List<ExportOption> = emptyList(),
    showNotifications: Boolean = false,
    notificationCount: Int = 0,
    onNotificationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val theme = rememberAppTheme()
    var showQuickActionsMenu by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = theme.colors.surface,
        shadowElevation = 4.dp
    ) {
        Column {
            // Main Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Section - Navigation and Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Back Button with animation
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = theme.colors.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Title and Subtitle
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = title,
                                color = theme.colors.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            subtitle?.let { subtitleText ->
                                Text(
                                    text = subtitleText,
                                    color = theme.colors.textSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            /*
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = theme.colors.primary,
                                trackColor = theme.colors.primaryContainer
                            )
                             */
                            // Status Indicator
                            statusIndicator?.let { status ->
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusBadge(status)
                            }
                        }

                        // Subtitle or Breadcrumbs
                        if (breadcrumbs.isNotEmpty()) {
                            BreadcrumbsRow(breadcrumbs, theme)
                        } else {
                            subtitle?.let {
                                Text(
                                    text = it,
                                    color = theme.colors.textSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Right Section - Actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search
                    searchConfig?.let { config ->
                        AnimatedVisibility(
                            visible = isSearchActive,
                            enter = expandHorizontally() + fadeIn(),
                            exit = shrinkHorizontally() + fadeOut()
                        ) {
                            SearchField(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                onSearch = { config.onSearch(searchQuery) },
                                onClose = {
                                    isSearchActive = false
                                    searchQuery = ""
                                },
                                placeholder = config.placeholder
                            )
                        }

                        if (!isSearchActive) {
                            IconButton(
                                onClick = { isSearchActive = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = theme.colors.textSecondary
                                )
                            }
                        }
                    }

                    // AI Assistant Toggle
                    if (onAiAssistantToggle != null) {
                        AiAssistantToggle(
                            enabled = aiAssistantEnabled,
                            onToggle = onAiAssistantToggle
                        )
                    }

                    // Notifications
                    if (showNotifications && onNotificationClick != null) {
                        NotificationButton(
                            count = notificationCount,
                            onClick = onNotificationClick
                        )
                    }

                    // Quick Actions Menu
                    if (quickActions.isNotEmpty()) {
                        Box {
                            IconButton(
                                onClick = { showQuickActionsMenu = !showQuickActionsMenu },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "More Actions",
                                    tint = theme.colors.textSecondary
                                )
                            }

                            DropdownMenu(
                                expanded = showQuickActionsMenu,
                                onDismissRequest = { showQuickActionsMenu = false }
                            ) {
                                quickActions.forEach { action ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Icon(
                                                    action.icon,
                                                    contentDescription = null,
                                                    tint = action.iconTint ?: theme.colors.textPrimary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(action.label)
                                            }
                                        },
                                        onClick = {
                                            action.onAction()
                                            showQuickActionsMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Export Options
                    if (exportOptions.isNotEmpty()) {
                        Box {
                            IconButton(
                                onClick = { showExportMenu = !showExportMenu },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Export/Share",
                                    tint = theme.colors.textSecondary
                                )
                            }

                            DropdownMenu(
                                expanded = showExportMenu,
                                onDismissRequest = { showExportMenu = false }
                            ) {
                                exportOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Icon(
                                                    option.icon,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Column {
                                                    Text(option.label)
                                                    option.description?.let {
                                                        Text(
                                                            it,
                                                            fontSize = 11.sp,
                                                            color = theme.colors.textSecondary
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        onClick = {
                                            option.onExport()
                                            showExportMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Custom Actions (backward compatibility)
                    actions()
                }
            }

            // Progress Indicator (if needed)
            statusIndicator?.let { status ->
                if (status.showProgress) {
                    LinearProgressIndicator(
                        progress = status.progress ?: 0f,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF4A90E2),
                        trackColor = Color(0xFF2A2A2A)
                    )
                }
            }
        }
    }
}

// ===== Supporting Data Classes and Components =====

/**
 * Breadcrumb data class
 */
data class Breadcrumb(
    val label: String,
    val onClick: (() -> Unit)? = null
)

/**
 * Search configuration
 */
data class SearchConfig(
    val placeholder: String = "Search...",
    val onSearch: (String) -> Unit
)

/**
 * Status indicator configuration
 */
data class StatusIndicator(
    val label: String,
    val type: StatusType,
    val showProgress: Boolean = false,
    val progress: Float? = null
)

enum class StatusType {
    SUCCESS, WARNING, ERROR, INFO, PROCESSING
}

/**
 * Quick action configuration
 */
data class QuickAction(
    val label: String,
    val icon: ImageVector,
    val onAction: () -> Unit,
    val iconTint: Color? = null
)

/**
 * Export option configuration
 */
data class ExportOption(
    val label: String,
    val icon: ImageVector,
    val description: String? = null,
    val onExport: () -> Unit
)


/**
 * Common action buttons for project screens
 */
@Composable
fun RowScope.ProjectScreenActions(
    onSearch: (() -> Unit)? = null,
    onFilter: (() -> Unit)? = null,
    onSettings: (() -> Unit)? = null,
    onMoreOptions: (() -> Unit)? = null,
    customActions: @Composable (RowScope.() -> Unit) = {}
) {
    val theme = rememberAppTheme()

    customActions()

    onSearch?.let {
        IconButton(onClick = it) {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = theme.colors.onSurface
            )
        }
    }

    onFilter?.let {
        IconButton(onClick = it) {
            Icon(
                Icons.Default.FilterList,
                contentDescription = "Filter",
                tint = theme.colors.onSurface
            )
        }
    }

    onSettings?.let {
        IconButton(onClick = it) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Settings",
                tint = theme.colors.onSurface
            )
        }
    }

    onMoreOptions?.let {
        IconButton(onClick = it) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = theme.colors.onSurface
            )
        }
    }
}


/**
 * Breadcrumbs Row Component
 */
@Composable
private fun BreadcrumbsRow(
    breadcrumbs: List<Breadcrumb>,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 2.dp)
    ) {
        breadcrumbs.forEachIndexed { index, breadcrumb ->
            val isLast = index == breadcrumbs.lastIndex

            Text(
                text = breadcrumb.label,
                color = if (isLast) theme.colors.textPrimary else theme.colors.textSecondary,
                fontSize = 12.sp,
                fontWeight = if (isLast) FontWeight.Medium else FontWeight.Normal,
                modifier = if (breadcrumb.onClick != null && !isLast) {
                    Modifier.clickable { breadcrumb.onClick.invoke() }
                } else Modifier
            )

            if (!isLast) {
                Text(
                    text = " / ",
                    color = theme.colors.textSecondary.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Status Badge Component
 */
@Composable
private fun StatusBadge(status: StatusIndicator) {
    val backgroundColor = when (status.type) {
        StatusType.SUCCESS -> Color(0xFF4CAF50).copy(alpha = 0.2f)
        StatusType.WARNING -> Color(0xFFFFA726).copy(alpha = 0.2f)
        StatusType.ERROR -> Color(0xFFEF5350).copy(alpha = 0.2f)
        StatusType.INFO -> Color(0xFF42A5F5).copy(alpha = 0.2f)
        StatusType.PROCESSING -> Color(0xFF9C27B0).copy(alpha = 0.2f)
    }

    val textColor = when (status.type) {
        StatusType.SUCCESS -> Color(0xFF4CAF50)
        StatusType.WARNING -> Color(0xFFFFA726)
        StatusType.ERROR -> Color(0xFFEF5350)
        StatusType.INFO -> Color(0xFF42A5F5)
        StatusType.PROCESSING -> Color(0xFF9C27B0)
    }

    val icon = when (status.type) {
        StatusType.SUCCESS -> Icons.Default.CheckCircle
        StatusType.WARNING -> Icons.Default.Warning
        StatusType.ERROR -> Icons.Default.Error
        StatusType.INFO -> Icons.Default.Info
        StatusType.PROCESSING -> Icons.Default.Sync
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (status.type == StatusType.PROCESSING) {
                val infiniteTransition = rememberInfiniteTransition()
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing)
                    )
                )
                Icon(
                    icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier
                        .size(14.dp)
                        .rotate(rotation)
                )
            } else {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            Text(
                text = status.label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Search Field Component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClose: () -> Unit,
    placeholder: String
) {
    val theme = rememberAppTheme()

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder, fontSize = 14.sp) },
        modifier = Modifier
            .width(200.dp)
            .height(40.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = theme.colors.surface,
            unfocusedContainerColor = theme.colors.surface,
            focusedBorderColor = theme.colors.primary,
            unfocusedBorderColor = theme.colors.surface.copy(alpha = 0.5f)
        ),
        trailingIcon = {
            Row {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close Search",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
            onSearch = { onSearch() }
        )
    )
}

/**
 * AI Assistant Toggle Component
 */
@Composable
private fun AiAssistantToggle(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val theme = rememberAppTheme()

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (enabled) Color(0xFF4A90E2).copy(alpha = 0.2f) else theme.colors.surface,
        modifier = Modifier
            .clickable { onToggle(!enabled) }
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = "AI Assistant",
                tint = if (enabled) Color(0xFF4A90E2) else theme.colors.textSecondary,
                modifier = Modifier.size(16.dp)
            )
            AnimatedVisibility(visible = enabled) {
                Text(
                    "AI",
                    color = Color(0xFF4A90E2),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Notification Button Component
 */
@Composable
private fun NotificationButton(
    count: Int,
    onClick: () -> Unit
) {
    val theme = rememberAppTheme()

    Box {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                if (count > 0) Icons.Default.Notifications else Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                tint = if (count > 0) Color(0xFFFFA726) else theme.colors.textSecondary
            )
        }

        if (count > 0) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFEF5350),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp)
            ) {
                Text(
                    text = if (count > 9) "9+" else count.toString(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(2.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

/**
 * Standard Top Bar for screens - DEPRECATED
 * Please use PrimaryTopBar for main navigation screens
 * or SecondaryTopBar for sub-screens
 */
@Deprecated(
    message = "Use PrimaryTopBar for main screens or SecondaryTopBar for sub-screens",
    replaceWith = ReplaceWith("PrimaryTopBar or SecondaryTopBar")
)
@Composable
fun StandardTopBar(
    title: String,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    // Redirect to appropriate new component
    if (onNavigateBack != null) {
        SecondaryTopBar(
            title = title,
            onNavigateBack = onNavigateBack,
            actions = actions
        )
    } else {
        PrimaryTopBar(
            title = title,
            actions = actions
        )
    }
}