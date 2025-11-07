package com.beekeeper.app.ui.screens.advisor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beekeeper.app.data.MockDataRepository
import com.beekeeper.app.domain.model.Alert
import com.beekeeper.app.domain.model.AlertSeverity
import com.beekeeper.app.domain.model.AlertType
import com.beekeeper.app.domain.model.WeatherCondition
import com.beekeeper.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAdvisorScreen(
    onBackClick: () -> Unit
) {
    val weather = MockDataRepository.weather
    val alerts = MockDataRepository.getActiveAlerts()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "AI Expert Advisor",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
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
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Weather section
            Text(
                "Weather for: Sunny Meadow Apiary",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            WeatherCard(weather = weather)

            // Today's AI Advice
            Text(
                "Today's AI Advice",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            AdviceCard(
                icon = Icons.Default.CheckCircle,
                iconColor = HealthyGreen,
                title = "Ideal day for a full hive inspection.",
                description = "Weather conditions are perfect. Check for queen health, brood pattern, and food stores.",
                backgroundColor = HealthyGreen.copy(alpha = 0.15f),
                borderColor = HealthyGreen.copy(alpha = 0.5f)
            )

            AdviceCard(
                icon = Icons.Default.Settings,
                iconColor = BeekeeperGold,
                title = "High nectar flow expected.",
                description = "Check for super space to avoid swarming. Add a new super to hives 01 and 03.",
                backgroundColor = BeekeeperGold.copy(alpha = 0.15f),
                borderColor = BeekeeperGold.copy(alpha = 0.5f)
            )

            // Upcoming Alerts & Reminders
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Upcoming Alerts & Reminders",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            UpcomingAlertCard(
                icon = Icons.Default.DateRange,
                iconColor = BeekeeperGold,
                title = "Swarm Season Begins",
                subtitle = "In 2 weeks",
                backgroundColor = BeekeeperGold.copy(alpha = 0.15f)
            )

            UpcomingAlertCard(
                icon = Icons.Default.Warning,
                iconColor = WarningOrange,
                title = "Mite treatment due",
                subtitle = "For Hive 02",
                backgroundColor = WarningOrange.copy(alpha = 0.15f)
            )

            UpcomingAlertCard(
                icon = Icons.Default.Info,
                iconColor = Color(0xFF42A5F5),
                title = "High humidity overnight",
                subtitle = "Ensure hive ventilation",
                backgroundColor = Color(0xFF42A5F5).copy(alpha = 0.15f)
            )
        }
    }
}

@Composable
fun WeatherCard(weather: com.beekeeper.app.domain.model.Weather) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Good conditions for hive work.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Icon(
                    imageVector = when (weather.condition) {
                        WeatherCondition.SUNNY -> Icons.Default.Star
                        WeatherCondition.PARTLY_CLOUDY -> Icons.Default.Star
                        WeatherCondition.CLOUDY -> Icons.Default.Star
                        WeatherCondition.RAINY -> Icons.Default.Star
                        WeatherCondition.STORMY -> Icons.Default.Warning
                    },
                    contentDescription = null,
                    tint = BeekeeperGold,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                weather.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            // Weather metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherMetric(
                    icon = Icons.Default.Star,
                    value = "${weather.temperature}°F",
                    label = ""
                )
                WeatherMetric(
                    icon = Icons.Default.Info,
                    value = "${weather.humidity}%",
                    label = ""
                )
                WeatherMetric(
                    icon = Icons.Default.Build,
                    value = "${weather.windSpeed} mph",
                    label = ""
                )
            }
        }
    }
}

@Composable
fun WeatherMetric(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        if (label.isNotEmpty()) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun AdviceCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    backgroundColor: Color,
    borderColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(borderColor)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun UpcomingAlertCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    backgroundColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}
