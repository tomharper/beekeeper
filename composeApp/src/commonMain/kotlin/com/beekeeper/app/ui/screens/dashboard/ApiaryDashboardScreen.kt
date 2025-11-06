package com.beekeeper.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beekeeper.app.data.MockDataRepository
import com.beekeeper.app.domain.model.Alert
import com.beekeeper.app.domain.model.AlertSeverity
import com.beekeeper.app.domain.model.Hive
import com.beekeeper.app.domain.model.HiveStatus
import com.beekeeper.app.ui.theme.*
import kotlinx.datetime.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiaryDashboardScreen(
    apiaryId: String,
    onHiveClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onNavigateToAdvisor: () -> Unit
) {
    val apiary = MockDataRepository.getApiaryById(apiaryId)
    val hives = MockDataRepository.getHivesForApiary(apiaryId)
    val alerts = MockDataRepository.getActiveAlerts()

    var showAlert by remember { mutableStateOf(alerts.isNotEmpty()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        apiary?.name ?: "Unknown Apiary",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BeekeeperGreenDark,
                    titleContentColor = TextPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                containerColor = BeekeeperGold,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Hive")
            }
        },
        bottomBar = {
            BottomNavigationBar(
                selectedTab = 0,
                onTabSelected = { tab ->
                    if (tab == 2) onNavigateToAdvisor()
                }
            )
        },
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Alert Banner
            if (showAlert && alerts.isNotEmpty()) {
                AlertBanner(
                    alert = alerts.first(),
                    onDismiss = { showAlert = false }
                )
            }

            // Hive Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(hives) { hive ->
                    HiveCard(
                        hive = hive,
                        onClick = { onHiveClick(hive.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AlertBanner(
    alert: Alert,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (alert.severity) {
                AlertSeverity.WARNING -> WarningOrange.copy(alpha = 0.15f)
                AlertSeverity.CRITICAL -> AlertRed.copy(alpha = 0.15f)
                AlertSeverity.INFO -> BeekeeperGold.copy(alpha = 0.15f)
            }
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(
                when (alert.severity) {
                    AlertSeverity.WARNING -> WarningOrange.copy(alpha = 0.5f)
                    AlertSeverity.CRITICAL -> AlertRed.copy(alpha = 0.5f)
                    AlertSeverity.INFO -> BeekeeperGold.copy(alpha = 0.5f)
                }
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = WarningOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BeekeeperGold.copy(alpha = 0.3f),
                    contentColor = BeekeeperGold
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
fun HiveCard(
    hive: Hive,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(0.85f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(BeekeeperGreenMedium),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = TextSecondary.copy(alpha = 0.3f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = hive.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (hive.status) {
                                HiveStatus.STRONG -> HealthyGreen.copy(alpha = 0.2f)
                                HiveStatus.ALERT -> AlertRed.copy(alpha = 0.2f)
                                HiveStatus.NEEDS_INSPECTION -> WarningOrange.copy(alpha = 0.2f)
                                HiveStatus.WEAK -> AlertRed.copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (hive.status) {
                            HiveStatus.STRONG -> "Strong"
                            HiveStatus.ALERT -> "Alert"
                            HiveStatus.NEEDS_INSPECTION -> "Needs Inspection"
                            HiveStatus.WEAK -> "Weak"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when (hive.status) {
                            HiveStatus.STRONG -> HealthyGreen
                            HiveStatus.ALERT -> AlertRed
                            HiveStatus.NEEDS_INSPECTION -> WarningOrange
                            HiveStatus.WEAK -> AlertRed
                        },
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Inspected: ${formatInspectionDate(hive.lastInspected)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = BeekeeperGreenDark,
        contentColor = TextPrimary
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Dashboard"
                )
            },
            label = { Text("Dashboard") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BeekeeperGold,
                selectedTextColor = BeekeeperGold,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = BeekeeperGold.copy(alpha = 0.2f)
            )
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "Tasks"
                )
            },
            label = { Text("Tasks") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BeekeeperGold,
                selectedTextColor = BeekeeperGold,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = BeekeeperGold.copy(alpha = 0.2f)
            )
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Advisor"
                )
            },
            label = { Text("Advisor") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BeekeeperGold,
                selectedTextColor = BeekeeperGold,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = BeekeeperGold.copy(alpha = 0.2f)
            )
        )
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            icon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BeekeeperGold,
                selectedTextColor = BeekeeperGold,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = BeekeeperGold.copy(alpha = 0.2f)
            )
        )
    }
}

fun formatInspectionDate(dateTime: LocalDateTime): String {
    val now = kotlinx.datetime.Clock.System.now()
    val inspectionInstant = kotlinx.datetime.TimeZone.currentSystemDefault().let { tz ->
        kotlinx.datetime.LocalDateTime(
            dateTime.year,
            dateTime.monthNumber,
            dateTime.dayOfMonth,
            dateTime.hour,
            dateTime.minute
        ).toInstant(tz)
    }

    val diff = now - inspectionInstant
    val days = diff.inWholeDays

    return when {
        days == 0L -> "today"
        days == 1L -> "1 day ago"
        days < 7 -> "$days days ago"
        days < 14 -> "1 week ago"
        days < 30 -> "${days / 7} weeks ago"
        else -> "${days / 30} months ago"
    }
}
