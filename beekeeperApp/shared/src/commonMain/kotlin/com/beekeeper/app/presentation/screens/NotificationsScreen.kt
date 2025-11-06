// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/NotificationsScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeper.app.domain.model.Notification
import com.beekeeper.app.domain.model.NotificationType
import com.beekeeper.app.domain.model.FeedAuthor
import com.beekeeper.app.presentation.components.SecondaryTopBar
import com.beekeeper.app.presentation.theme.ThemeManager
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    onNotificationClick: (Notification) -> Unit = {}
) {
    val theme by ThemeManager.currentTheme.collectAsState()

    // Sample notifications - replace with real data from repository
    var notifications by remember {
        mutableStateOf(
            listOf(
                Notification(
                    id = "1",
                    type = NotificationType.AI_GENERATION_COMPLETE,
                    title = "Script Generated",
                    message = "Your script for 'Winnie the Pooh Episode 1' is ready!",
                    targetId = "script_1",
                    targetType = "script",
                    timestamp = Clock.System.now(),
                    isRead = false,
                    actor = null
                ),
                Notification(
                    id = "2",
                    type = NotificationType.PROJECT_UPDATE,
                    title = "Project Updated",
                    message = "New character 'Eeyore' added to your project",
                    targetId = "project_1",
                    targetType = "project",
                    timestamp = Clock.System.now(),
                    isRead = false,
                    actor = FeedAuthor(
                        id = "user_1",
                        username = "teammate",
                        displayName = "Your Teammate",
                        avatarUrl = null
                    )
                ),
                Notification(
                    id = "3",
                    type = NotificationType.COMMENT,
                    title = "New Comment",
                    message = "Sarah commented on your storyboard",
                    targetId = "post_1",
                    targetType = "post",
                    timestamp = Clock.System.now(),
                    isRead = true,
                    actor = FeedAuthor(
                        id = "sarah_1",
                        username = "sarah",
                        displayName = "Sarah Johnson",
                        avatarUrl = null
                    )
                ),
                Notification(
                    id = "4",
                    type = NotificationType.LIKE,
                    title = "Post Liked",
                    message = "John liked your latest scene",
                    targetId = "post_2",
                    targetType = "post",
                    timestamp = Clock.System.now(),
                    isRead = true,
                    actor = FeedAuthor(
                        id = "john_1",
                        username = "john",
                        displayName = "John Smith",
                        avatarUrl = null
                    )
                ),
                Notification(
                    id = "5",
                    type = NotificationType.SYSTEM,
                    title = "System Update",
                    message = "CineFiller has been updated to v1.2.0",
                    targetId = "system",
                    targetType = "system",
                    timestamp = Clock.System.now(),
                    isRead = true,
                    actor = null
                )
            )
        )
    }

    val unreadCount = notifications.count { !it.isRead }

    Scaffold(
        topBar = {
            SecondaryTopBar(
                title = "Notifications",
                subtitle = if (unreadCount > 0) "$unreadCount unread" else "All caught up!",
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = theme.colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mark all as read button
            if (unreadCount > 0) {
                TextButton(
                    onClick = {
                        notifications = notifications.map { it.copy(isRead = true) }
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Mark all as read",
                        color = theme.colors.primary,
                        fontSize = 14.sp
                    )
                }
            }

            if (notifications.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = theme.colors.textSecondary.copy(alpha = 0.5f)
                        )
                        Text(
                            "No notifications",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = theme.colors.textSecondary
                        )
                        Text(
                            "You're all caught up!",
                            fontSize = 14.sp,
                            color = theme.colors.textSecondary.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(notifications.size) { index ->
                        val notification = notifications[index]
                        NotificationCard(
                            notification = notification,
                            onClick = {
                                // Mark as read when clicked
                                notifications = notifications.map {
                                    if (it.id == notification.id) it.copy(isRead = true) else it
                                }
                                onNotificationClick(notification)
                            },
                            onDismiss = {
                                notifications = notifications.filter { it.id != notification.id }
                            },
                            theme = theme
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) {
                theme.colors.surface
            } else {
                theme.colors.primary.copy(alpha = 0.05f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.isRead) 1.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon based on notification type
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(getNotificationColor(notification.type, theme).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getNotificationIcon(notification.type),
                    contentDescription = null,
                    tint = getNotificationColor(notification.type, theme),
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 16.sp,
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                        color = theme.colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(theme.colors.primary)
                        )
                    }
                }

                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = theme.colors.textSecondary,
                    lineHeight = 20.sp
                )

                notification.actor?.let { actor ->
                    Text(
                        text = "by ${actor.displayName}",
                        fontSize = 12.sp,
                        color = theme.colors.textSecondary.copy(alpha = 0.7f)
                    )
                }

                Text(
                    text = getTimeAgo(notification.timestamp),
                    fontSize = 12.sp,
                    color = theme.colors.textSecondary.copy(alpha = 0.6f)
                )
            }

            // Dismiss button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = theme.colors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun getNotificationIcon(type: NotificationType): ImageVector {
    return when (type) {
        NotificationType.LIKE -> Icons.Default.Favorite
        NotificationType.COMMENT -> Icons.Default.Comment
        NotificationType.FOLLOW -> Icons.Default.PersonAdd
        NotificationType.MENTION -> Icons.Default.AlternateEmail
        NotificationType.SHARE -> Icons.Default.Share
        NotificationType.PROJECT_UPDATE -> Icons.Default.Update
        NotificationType.AI_GENERATION_COMPLETE -> Icons.Default.AutoAwesome
        NotificationType.SYSTEM -> Icons.Default.Info
    }
}

private fun getNotificationColor(
    type: NotificationType,
    theme: com.beekeeper.app.presentation.theme.AppTheme
): Color {
    return when (type) {
        NotificationType.LIKE -> theme.colors.error
        NotificationType.COMMENT -> theme.colors.info
        NotificationType.FOLLOW -> theme.colors.success
        NotificationType.MENTION -> theme.colors.warning
        NotificationType.SHARE -> theme.colors.primary
        NotificationType.PROJECT_UPDATE -> theme.colors.secondary
        NotificationType.AI_GENERATION_COMPLETE -> theme.colors.tertiary
        NotificationType.SYSTEM -> theme.colors.textSecondary
    }
}

private fun getTimeAgo(timestamp: kotlinx.datetime.Instant): String {
    val now = Clock.System.now()
    val duration = now - timestamp

    return when {
        duration.inWholeMinutes < 1 -> "Just now"
        duration.inWholeMinutes < 60 -> "${duration.inWholeMinutes}m ago"
        duration.inWholeHours < 24 -> "${duration.inWholeHours}h ago"
        duration.inWholeDays < 7 -> "${duration.inWholeDays}d ago"
        else -> "${duration.inWholeDays / 7}w ago"
    }
}
