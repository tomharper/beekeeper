package com.beekeeper.app.ui.screens.apiary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beekeeper.app.data.MockDataRepository
import com.beekeeper.app.domain.model.Apiary
import com.beekeeper.app.domain.model.ApiaryStatus
import com.beekeeper.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiaryListScreen(
    onApiaryClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Apiaries",
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
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
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
                Icon(Icons.Default.Add, contentDescription = "Add Apiary")
            }
        },
        containerColor = BackgroundDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text(
                    "Apiary Sites Overview",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(MockDataRepository.apiaries) { apiary ->
                ApiaryCard(
                    apiary = apiary,
                    onClick = { onApiaryClick(apiary.id) }
                )
            }
        }
    }
}

@Composable
fun ApiaryCard(
    apiary: Apiary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = apiary.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = apiary.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${apiary.hiveCount} Hives",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Status indicator
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when (apiary.status) {
                                ApiaryStatus.HEALTHY -> HealthyGreen.copy(alpha = 0.2f)
                                ApiaryStatus.WARNING -> WarningOrange.copy(alpha = 0.2f)
                                ApiaryStatus.ALERT -> AlertRed.copy(alpha = 0.2f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (apiary.status) {
                            ApiaryStatus.HEALTHY -> Icons.Default.Check
                            ApiaryStatus.WARNING -> Icons.Default.Warning
                            ApiaryStatus.ALERT -> Icons.Default.Close
                        },
                        contentDescription = apiary.status.name,
                        modifier = Modifier.size(20.dp),
                        tint = when (apiary.status) {
                            ApiaryStatus.HEALTHY -> HealthyGreen
                            ApiaryStatus.WARNING -> WarningOrange
                            ApiaryStatus.ALERT -> AlertRed
                        }
                    )
                }

                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = "Navigate",
                    tint = TextSecondary
                )
            }
        }
    }
}
