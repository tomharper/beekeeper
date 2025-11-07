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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
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
                        Text("AI Beekeeping Advisor", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Powered by Claude", fontSize = 12.sp, color = Color(0xFF888888))
                    }
                },
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, "Back")
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
