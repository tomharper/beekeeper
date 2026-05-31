package com.beekeeper.app.ui.screens.feed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.beekeeper.app.domain.model.FeedItem
import com.beekeeper.app.domain.model.FeedItemType
import com.beekeeper.app.domain.model.FollowUser
import com.beekeeper.app.ui.viewmodel.FeedViewModel
import kotlinx.datetime.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    onBackClick: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSearch by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Following") },
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(
                            Icons.Default.PersonAdd,
                            contentDescription = "Find beekeepers",
                            tint = Color(0xFFFFD700)
                        )
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
            if (showSearch) {
                FollowSearchSection(
                    query = uiState.searchQuery,
                    results = uiState.searchResults,
                    isSearching = uiState.isSearching,
                    isFollowing = { viewModel.isFollowing(it) },
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    onSearch = { viewModel.search() },
                    onFollow = { viewModel.follow(it) },
                    onUnfollow = { viewModel.unfollow(it) }
                )
                Divider(color = Color(0xFF2A2A2A))
            }

            when {
                uiState.isLoading && uiState.feed.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFFFD700))
                    }
                }

                uiState.error != null && uiState.feed.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.error ?: "An error occurred",
                                color = Color(0xFFFF6B6B)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadFeed() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFD700),
                                    contentColor = Color.Black
                                )
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }

                uiState.feed.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No activity yet. Follow other beekeepers to see their public inspections and tasks.",
                            color = Color(0xFF888888),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.feed, key = { it.id }) { item ->
                            FeedItemCard(item)
                        }
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { viewModel.loadMore() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (uiState.isLoading) "Loading..." else "Load more",
                                    color = Color(0xFFFFD700)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FollowSearchSection(
    query: String,
    results: List<FollowUser>,
    isSearching: Boolean,
    isFollowing: (String) -> Boolean,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onFollow: (String) -> Unit,
    onUnfollow: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search beekeepers by name", color = Color(0xFF888888)) },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = onSearch) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFFFFD700))
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFFFD700),
                unfocusedBorderColor = Color(0xFF2A2A2A),
                cursorColor = Color(0xFFFFD700)
            )
        )

        if (isSearching) {
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator(
                color = Color(0xFFFFD700),
                modifier = Modifier.size(20.dp)
            )
        }

        results.forEach { user ->
            Spacer(modifier = Modifier.height(8.dp))
            UserRow(
                user = user,
                following = isFollowing(user.id),
                onFollow = { onFollow(user.id) },
                onUnfollow = { onUnfollow(user.id) }
            )
        }
    }
}

@Composable
private fun UserRow(
    user: FollowUser,
    following: Boolean,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = user.fullName,
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        if (following) {
            OutlinedButton(onClick = onUnfollow) {
                Text("Following", color = Color(0xFF888888))
            }
        } else {
            Button(
                onClick = onFollow,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700),
                    contentColor = Color.Black
                )
            ) {
                Text("Follow")
            }
        }
    }
}

@Composable
private fun FeedItemCard(item: FeedItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.author.fullName,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatFeedDate(item.occurredAt),
                    color = Color(0xFF888888),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Type tag
            Surface(
                color = Color(0xFF2A2A2A),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (item.type == FeedItemType.TASK) "TASK" else "INSPECTION",
                    color = Color(0xFFFFD700),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.summary,
                color = Color(0xFFCCCCCC),
                fontSize = 14.sp
            )
        }
    }
}

private fun formatFeedDate(date: LocalDateTime): String {
    return "${date.monthNumber}/${date.dayOfMonth}/${date.year}"
}
