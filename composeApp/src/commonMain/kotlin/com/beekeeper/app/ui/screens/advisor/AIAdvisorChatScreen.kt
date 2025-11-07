package com.beekeeper.app.ui.screens.advisor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beekeeper.app.domain.model.Alert
import com.beekeeper.app.domain.model.AlertSeverity
import com.beekeeper.app.domain.model.ChatMessage
import com.beekeeper.app.domain.model.MessageRole
import com.beekeeper.app.ui.viewmodel.AIAdvisorViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAdvisorChatScreen(
    viewModel: AIAdvisorViewModel,
    onBackClick: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showChat by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty() && showChat) {
            coroutineScope.launch {
                listState.animateScrollToItem(uiState.messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (!showChat) "AI Advisor" else "AI Beekeeping Advisor",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (!showChat) "Smart alerts & expert advice" else "Powered by Claude",
                            fontSize = 12.sp,
                            color = Color(0xFF888888)
                        )
                    }
                },
                navigationIcon = {
                    if (onBackClick != null && !showChat) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    } else if (showChat) {
                        IconButton(onClick = { showChat = false }) {
                            Icon(Icons.Default.ArrowBack, "Back to Alerts")
                        }
                    }
                },
                actions = {
                    if (!showChat) {
                        IconButton(onClick = { viewModel.loadAlerts() }) {
                            Icon(Icons.Default.Refresh, "Refresh", tint = Color(0xFFFFD700))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0F0F0F)
    ) { padding ->
        if (!showChat) {
            // Alerts View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Section header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Active Alerts & Recommendations",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    "Based on your hive data and seasonal patterns",
                                    fontSize = 13.sp,
                                    color = Color(0xFF888888)
                                )
                            }
                        }
                    }

                    // Loading state
                    if (uiState.isLoadingAlerts) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFFFFD700),
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }

                    // Alerts list
                    if (!uiState.isLoadingAlerts && uiState.alerts.isNotEmpty()) {
                        items(uiState.alerts, key = { it.id }) { alert ->
                            AlertCard(alert)
                        }
                    }

                    // Empty state
                    if (!uiState.isLoadingAlerts && uiState.alerts.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                color = Color(0xFF1F3D1F),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        "All Good!",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4CAF50)
                                    )
                                    Text(
                                        "No active alerts at this time. Your hives are looking good!",
                                        fontSize = 14.sp,
                                        color = Color(0xFF888888)
                                    )
                                }
                            }
                        }
                    }
                }

                // "Ask AI Expert" button
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF1A1A1A),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Divider(color = Color(0xFF333333))
                        Button(
                            onClick = { showChat = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFD700),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Ask AI Expert",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            "Have questions? Chat with our AI beekeeping expert",
                            fontSize = 12.sp,
                            color = Color(0xFF888888),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            // Chat View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Messages list
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.messages, key = { it.id }) { message ->
                        ChatMessageBubble(message)
                    }

                    // Loading indicator
                    if (uiState.isLoading) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Surface(
                                    color = Color(0xFF2A2A2A),
                                    shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = Color(0xFFFFD700)
                                        )
                                        Text("Thinking...", color = Color(0xFF888888), fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Error message
                    if (uiState.error != null) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF3D1F1F),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFFF6B6B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        uiState.error ?: "An error occurred",
                                        color = Color(0xFFFF6B6B),
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { viewModel.clearError() }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Dismiss",
                                            tint = Color(0xFFFF6B6B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Input area
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF1A1A1A),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Ask about beekeeping...", color = Color(0xFF888888)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFF444444),
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                cursorColor = Color(0xFFFFD700)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            minLines = 1,
                            maxLines = 4
                        )

                        FloatingActionButton(
                            onClick = {
                                if (messageText.isNotBlank() && !uiState.isLoading) {
                                    viewModel.sendMessage(messageText)
                                    messageText = ""
                                }
                            },
                            containerColor = if (messageText.isBlank() || uiState.isLoading)
                                Color(0xFF444444) else Color(0xFFFFD700),
                            contentColor = if (messageText.isBlank() || uiState.isLoading)
                                Color(0xFF888888) else Color.Black,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Send",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlertCard(alert: Alert) {
    val (icon, iconColor, bgColor) = when (alert.severity) {
        AlertSeverity.CRITICAL -> Triple(
            Icons.Default.Warning,
            Color(0xFFFF6B6B),
            Color(0xFF3D1F1F)
        )
        AlertSeverity.WARNING -> Triple(
            Icons.Default.Info,
            Color(0xFFFF9F40),
            Color(0xFF3D2F1F)
        )
        AlertSeverity.INFO -> Triple(
            Icons.Default.Info,
            Color(0xFF2196F3),
            Color(0xFF1F2F3D)
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    alert.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    alert.message,
                    fontSize = 14.sp,
                    color = Color(0xFFCCCCCC),
                    lineHeight = 20.sp
                )
                if (alert.hiveIds.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        alert.hiveIds.forEach { hiveId ->
                            Surface(
                                color = iconColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "Hive $hiveId",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    color = iconColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessage) {
    val isUser = message.role == MessageRole.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isUser) Color(0xFFFFD700) else Color(0xFF2A2A2A),
            shape = if (isUser)
                RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
            else
                RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = message.content,
                    color = if (isUser) Color.Black else Color.White,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )
                Text(
                    text = formatTime(message.timestamp),
                    color = if (isUser) Color.Black.copy(alpha = 0.6f) else Color(0xFF888888),
                    fontSize = 11.sp
                )
            }
        }
    }
}

fun formatTime(dateTime: kotlinx.datetime.LocalDateTime): String {
    val hour = if (dateTime.hour == 0) 12 else if (dateTime.hour > 12) dateTime.hour - 12 else dateTime.hour
    val amPm = if (dateTime.hour < 12) "AM" else "PM"
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "$hour:$minute $amPm"
}
