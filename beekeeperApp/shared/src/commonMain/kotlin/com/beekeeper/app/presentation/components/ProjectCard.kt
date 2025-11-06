// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/components/ProjectCard.kt
package com.beekeeper.app.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.presentation.screens.HomeQuickAction

@Composable
fun ProjectCard(
    project: CreativeProject,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = project.type.name.replace("_", " "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status = project.status)
            }

            // Progress
            val progress = calculateProjectProgress(project)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(progress * 100).toInt()}% Complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                project.timeline?.plannedEndDate?.let { deadline ->
                    Text(
                        text = "Due ${formatDeadline(deadline)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: ProjectStatus) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = getStatusColor(status).copy(alpha = 0.2f)
    ) {
        Text(
            text = status.name.replace("_", " "),
            style = MaterialTheme.typography.labelSmall,
            color = getStatusColor(status),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.primary,
    subtitle: String? = null
) {
    Card(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = color
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// QuickAction data class for ProjectDashboardScreen
data class QuickActionProject(
    val title: String,
    val icon: ImageVector
)

@Composable
fun QuickActionCardProject(
    action: QuickAction,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = action.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Helper functions
private fun calculateProjectProgress(project: CreativeProject): Float {
    // Simple progress calculation based on phase
    return when (project.currentPhase) {
        ProductionPhase.DEVELOPMENT -> 0.2f
        ProductionPhase.PRE_PRODUCTION -> 0.4f
        ProductionPhase.PRODUCTION -> 0.6f
        ProductionPhase.POST_PRODUCTION -> 0.8f
        ProductionPhase.DISTRIBUTION -> 1.0f
    }
}

private fun formatDeadline(endDate: kotlinx.datetime.Instant): String {
    // Simple formatting - you can enhance this
    return "Soon"
}

private fun getStatusColor(status: ProjectStatus): Color {
    return when (status) {
        ProjectStatus.PLANNING -> Color(0xFF2196F3)
        ProjectStatus.IN_DEVELOPMENT -> Color(0xFF4CAF50)
        ProjectStatus.PRE_PRODUCTION -> Color(0xFFFF9800)
        ProjectStatus.IN_PRODUCTION -> Color(0xFF9C27B0)
        ProjectStatus.POST_PRODUCTION -> Color(0xFF607D8B)
        ProjectStatus.IN_REVIEW -> Color(0xFFFFEB3B)
        ProjectStatus.READY_FOR_DISTRIBUTION -> Color(0xFF8BC34A)
        ProjectStatus.DELIVERED -> Color(0xFF4CAF50)
        ProjectStatus.ARCHIVED -> Color(0xFF757575)
        ProjectStatus.CANCELLED -> Color(0xFFF44336)
        ProjectStatus.DRAFT -> Color(0xFFFFEB3B)
        ProjectStatus.COMPLETED ->Color(0xFF8BC34A)
        ProjectStatus.PUBLISHED -> Color(0xFF4CAF50)
        ProjectStatus.ON_HOLD -> Color(0xFFF44336)
        ProjectStatus.ACTIVE -> Color(0xFF9C27B0)
    }
}



data class QuickActionItem(
    val type: HomeQuickActionList,
    val label: String,
    val icon: ImageVector
)

data class StatItem(
    val label: String,
    val value: String,
    val icon: ImageVector
)
// Quick Action enum

private val quickActionItems = listOf(
    QuickActionItem(HomeQuickActionList.SOCIAL_MEDIA_POST, "Social Post", Icons.Default.Share),
    QuickActionItem(HomeQuickActionList.GENERATE_SCRIPT, "Script", Icons.Default.Description),
    QuickActionItem(HomeQuickActionList.CREATE_AVATAR, "Avatar", Icons.Default.Face),
    QuickActionItem(HomeQuickActionList.IMPORT_ASSETS, "Import", Icons.Default.Upload)
)

// Data classes
enum class HomeQuickActionList {
    NEW_PROJECT,
    SOCIAL_MEDIA_POST,
    GENERATE_SCRIPT,
    CREATE_AVATAR,
    BATCH_EXPORT,
    IMPORT_ASSETS
}
