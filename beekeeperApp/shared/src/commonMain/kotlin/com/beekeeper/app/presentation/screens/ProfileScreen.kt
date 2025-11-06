// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/ProfileScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeper.app.domain.model.User
import com.beekeeper.app.presentation.components.PrimaryTopBar
import com.beekeeper.app.presentation.theme.ThemeManager



data class ProfileSection(
    val title: String,
    val icon: ImageVector,
    val items: List<ProfileMenuItem>
)

data class ProfileMenuItem(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val action: ProfileAction,
    val badge: String? = null
)

enum class ProfileAction {
    EDIT_PROFILE,
    CHANGE_PASSWORD,
    NOTIFICATION_SETTINGS,
    PRIVACY_SETTINGS,
    LANGUAGE,
    THEME,
    SUBSCRIPTION,
    BILLING,
    HELP_CENTER,
    CONTACT_SUPPORT,
    ABOUT,
    TERMS,
    PRIVACY_POLICY,
    LOGOUT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onActionClick: (ProfileAction) -> Unit = {}
) {
    val theme by ThemeManager.currentTheme.collectAsState()
    val userProfile = remember { User(
        id = "doofoo",
        username = "foo",
        displayName = "tom foo"
    ) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Create profile sections with dynamic theme subtitle
    val profileSections = remember(theme.isDarkMode) { listOf(
        ProfileSection(
            title = "Account Settings",
            icon = Icons.Default.AccountCircle,
            items = listOf(
                ProfileMenuItem(
                    title = "Edit Profile",
                    subtitle = "Update your personal information",
                    icon = Icons.Default.Edit,
                    action = ProfileAction.EDIT_PROFILE
                ),
                ProfileMenuItem(
                    title = "Change Password",
                    subtitle = "Update your security credentials",
                    icon = Icons.Default.Lock,
                    action = ProfileAction.CHANGE_PASSWORD
                ),
                ProfileMenuItem(
                    title = "Notification Settings",
                    subtitle = "Manage your notifications",
                    icon = Icons.Default.Notifications,
                    action = ProfileAction.NOTIFICATION_SETTINGS,
                    badge = "3"
                ),
                ProfileMenuItem(
                    title = "Privacy Settings",
                    subtitle = "Control your data and privacy",
                    icon = Icons.Default.Security,
                    action = ProfileAction.PRIVACY_SETTINGS
                )
            )
        ),
        ProfileSection(
            title = "Preferences",
            icon = Icons.Default.Settings,
            items = listOf(
                ProfileMenuItem(
                    title = "Language",
                    subtitle = "English (US)",
                    icon = Icons.Default.Language,
                    action = ProfileAction.LANGUAGE
                ),
                ProfileMenuItem(
                    title = "Theme",
                    subtitle = if (theme.isDarkMode) "Dark Mode" else "Light Mode",
                    icon = Icons.Default.Palette,
                    action = ProfileAction.THEME
                )
            )
        ),
        ProfileSection(
            title = "Subscription & Billing",
            icon = Icons.Default.CreditCard,
            items = listOf(
                ProfileMenuItem(
                    title = "Manage Subscription",
                    subtitle = userProfile.subscription.displayName + " Plan",
                    icon = Icons.Default.Diamond,
                    action = ProfileAction.SUBSCRIPTION
                ),
                ProfileMenuItem(
                    title = "Billing History",
                    subtitle = "View your payment history",
                    icon = Icons.Default.Receipt,
                    action = ProfileAction.BILLING
                )
            )
        ),
        ProfileSection(
            title = "Support",
            icon = Icons.Default.Help,
            items = listOf(
                ProfileMenuItem(
                    title = "Help Center",
                    subtitle = "Browse FAQs and guides",
                    icon = Icons.Default.HelpOutline,
                    action = ProfileAction.HELP_CENTER
                ),
                ProfileMenuItem(
                    title = "Contact Support",
                    subtitle = "Get help from our team",
                    icon = Icons.Default.SupportAgent,
                    action = ProfileAction.CONTACT_SUPPORT
                )
            )
        ),
        ProfileSection(
            title = "Legal",
            icon = Icons.Default.Gavel,
            items = listOf(
                ProfileMenuItem(
                    title = "About CineFiller",
                    icon = Icons.Default.Info,
                    action = ProfileAction.ABOUT
                ),
                ProfileMenuItem(
                    title = "Terms of Service",
                    icon = Icons.Default.Description,
                    action = ProfileAction.TERMS
                ),
                ProfileMenuItem(
                    title = "Privacy Policy",
                    icon = Icons.Default.PrivacyTip,
                    action = ProfileAction.PRIVACY_POLICY
                )
            )
        )
    ) }

    Scaffold(
        topBar = {
            PrimaryTopBar(
                title = "Profile",
                subtitle = "Manage your account and preferences",
                unreadNotificationCount = 3,
                onNotificationClick = onNavigateToNotifications,
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = theme.colors.textPrimary
                        )
                    }
                }
            )
        },
        containerColor = theme.colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Header
            ProfileHeader(userProfile)

            // Stats Section
            ProfileStatsSection(userProfile)

            // Profile Sections
            profileSections.forEach { section ->
                ProfileSectionCard(
                    section = section,
                    onItemClick = { action ->
                        when (action) {
                            ProfileAction.LOGOUT -> showLogoutDialog = true
                            ProfileAction.THEME -> ThemeManager.toggleTheme()
                            else -> onActionClick(action)
                        }
                    }
                )
            }

            // Logout Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { showLogoutDialog = true },
                colors = CardDefaults.cardColors(
                    containerColor = theme.colors.error.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = theme.colors.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Sign Out",
                        color = theme.colors.error,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }

            // Version Info
            Text(
                "CineFiller v1.0.0",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center,
                color = theme.colors.textSecondary,
                fontSize = 12.sp
            )
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out", color = theme.colors.textPrimary) },
            text = {
                Text(
                    "Are you sure you want to sign out?",
                    color = theme.colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onActionClick(ProfileAction.LOGOUT)
                    }
                ) {
                    Text("Sign Out", color = theme.colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = theme.colors.textSecondary)
                }
            },
            containerColor = theme.colors.surface
        )
    }
}

@Composable
private fun ProfileHeader(userProfile: User) {
    val theme by ThemeManager.currentTheme.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = theme.colors.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(theme.colors.primary.copy(alpha = 0.1f))
                    .border(3.dp, theme.colors.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = theme.colors.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Info
            Text(
                userProfile.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary
            )

            Text(
                userProfile.email,
                fontSize = 14.sp,
                color = theme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subscription Badge
            Badge(
                containerColor = userProfile.subscription.color.copy(alpha = 0.2f),
                contentColor = userProfile.subscription.color
            ) {
                Text(
                    userProfile.subscription.displayName.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Text(
                userProfile.joinDate,
                fontSize = 12.sp,
                color = theme.colors.textSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ProfileStatsSection(user: User) {
    val theme by ThemeManager.currentTheme.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = theme.colors.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("Projects", user.projectsCreated.toString(), Icons.Default.Folder)
            StatItem("Videos", user.videosPublished.toString(), Icons.Default.VideoLibrary)
            StatItem("Views", user.totalViews, Icons.Default.Visibility)
            StatItem("Storage", user.storageUsed, Icons.Default.Storage)
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, icon: ImageVector) {
    val theme by ThemeManager.currentTheme.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = theme.colors.primary
        )
        Text(
            value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = theme.colors.textPrimary
        )
        Text(
            label,
            fontSize = 12.sp,
            color = theme.colors.textSecondary
        )
    }
}

@Composable
private fun ProfileSectionCard(
    section: ProfileSection,
    onItemClick: (ProfileAction) -> Unit
) {
    val theme by ThemeManager.currentTheme.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = theme.colors.surface)
    ) {
        Column {
            // Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    section.icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = theme.colors.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    section.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = theme.colors.textPrimary
                )
            }

            Divider(color = theme.colors.surface)

            // Section Items
            section.items.forEach { item ->
                ProfileMenuItemRow(
                    item = item,
                    onClick = { onItemClick(item.action) }
                )
            }
        }
    }
}

@Composable
private fun ProfileMenuItemRow(
    item: ProfileMenuItem,
    onClick: () -> Unit
) {
    val theme by ThemeManager.currentTheme.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            item.icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = theme.colors.textSecondary
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                item.title,
                fontSize = 14.sp,
                color = theme.colors.textPrimary
            )
            item.subtitle?.let {
                Text(
                    it,
                    fontSize = 12.sp,
                    color = theme.colors.textSecondary
                )
            }
        }

        item.badge?.let {
            Badge(
                containerColor = theme.colors.error
            ) {
                Text(it, fontSize = 10.sp)
            }
        }

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = theme.colors.textSecondary
        )
    }
}