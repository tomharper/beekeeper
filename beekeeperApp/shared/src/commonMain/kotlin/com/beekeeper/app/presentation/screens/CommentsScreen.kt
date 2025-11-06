// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/CommentsScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.beekeeper.app.domain.model.FeedAuthor
import com.beekeeper.app.domain.model.ProjectReference
import com.beekeeper.app.utils.formatTimestamp
import com.beekeeper.app.utils.getCurrentTimeMillis
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

data class Comment(
    val id: String,
    val author: FeedAuthor,
    val text: String,
    val timestamp: Instant,
    val likes: Int,
    val hasLiked: Boolean,
    val replies: List<Comment> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    postId: String,
    onNavigateBack: () -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf(generateMockComments()) }
    var replyingTo by remember { mutableStateOf<Comment?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comments") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            CommentInputBar(
                value = commentText,
                onValueChange = { commentText = it },
                onSend = {
                    if (commentText.isNotBlank()) {
                        // Add comment
                        val newComment = Comment(
                            id = "comment_${getCurrentTimeMillis()}",
                            author = FeedAuthor(
                                id = "current_user",
                                username = "currentuser",
                                displayName = "You",
                                avatarUrl = null,
                                isVerified = false,
                                isFollowing = false
                            ),
                            text = commentText,
                            timestamp = Clock.System.now(),
                            likes = 0,
                            hasLiked = false
                        )
                        comments = listOf(newComment) + comments
                        commentText = ""
                    }
                },
                replyingTo = replyingTo,
                onCancelReply = { replyingTo = null }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(comments) { comment ->
                CommentItem(
                    comment = comment,
                    onLike = { /* Handle like */ },
                    onReply = { replyingTo = comment }
                )
                
                // Show replies
                comment.replies.forEach { reply ->
                    CommentItem(
                        comment = reply,
                        onLike = { /* Handle like */ },
                        onReply = { replyingTo = reply },
                        isReply = true
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentItem(
    comment: Comment,
    onLike: () -> Unit,
    onReply: () -> Unit,
    isReply: Boolean = false
) {
    var hasLiked by remember { mutableStateOf(comment.hasLiked) }
    var likeCount by remember { mutableStateOf(comment.likes) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isReply) 56.dp else 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 8.dp
            )
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.align(Alignment.Center),
                tint = Color.White
            )
        }
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            // Author and timestamp
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    comment.author.displayName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                if (comment.author.isVerified) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = Color(0xFF1DA1F2),
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(14.dp)
                    )
                }
                Text(
                    " · ${formatTimestamp(comment.timestamp)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Comment text
            Text(
                comment.text,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            // Actions
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Like
                Row(
                    modifier = Modifier.clickable {
                        hasLiked = !hasLiked
                        likeCount = if (hasLiked) likeCount + 1 else likeCount - 1
                        onLike()
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (hasLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like",
                        modifier = Modifier.size(16.dp),
                        tint = if (hasLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (likeCount > 0) {
                        Text(
                            " $likeCount",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Reply
                Text(
                    "Reply",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { onReply() }
                )
            }
        }
    }
}

@Composable
private fun CommentInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    replyingTo: Comment?,
    onCancelReply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Reply indicator
        if (replyingTo != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Replying to ${replyingTo.author.displayName}",
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onCancelReply,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cancel reply",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        // Input field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("Add a comment...") },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp)
            )
            
            IconButton(
                onClick = onSend,
                enabled = value.isNotBlank()
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (value.isNotBlank()) 
                        Color(0xFF7C3AED) 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/CreatePostScreen.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    initialMediaUri: String? = null,
    onNavigateBack: () -> Unit,
    onPostCreated: () -> Unit
) {
    var postText by remember { mutableStateOf("") }
    var selectedMedia by remember { mutableStateOf<List<String>>(
        initialMediaUri?.let { listOf(it) } ?: emptyList()
    ) }
    var selectedProject by remember { mutableStateOf<ProjectReference?>(null) }
    var isAiGenerated by remember { mutableStateOf(false) }
    var isPosting by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Post") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, "Close")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            isPosting = true
                            // Simulate post creation
                            onPostCreated()
                        },
                        enabled = postText.isNotBlank() || selectedMedia.isNotEmpty()
                    ) {
                        Text(
                            "Post",
                            color = if (postText.isNotBlank() || selectedMedia.isNotEmpty())
                                Color(0xFF7C3AED)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Text input
            TextField(
                value = postText,
                onValueChange = { postText = it },
                placeholder = { Text("What's on your mind?") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 10
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Selected media preview
            if (selectedMedia.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedMedia.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Gray)
                        ) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.Center),
                                tint = Color.White
                            )
                            
                            // Remove button
                            IconButton(
                                onClick = {
                                    selectedMedia = selectedMedia.filterIndexed { i, _ -> i != index }
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .background(
                                        Color.Black.copy(alpha = 0.5f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Selected project
            if (selectedProject != null) {
                ProjectReferenceCard(
                    project = selectedProject!!,
                    onClick = { /* Handle project click */ },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // AI Generated toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF7C3AED),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "AI Generated Content",
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                )
                Switch(
                    checked = isAiGenerated,
                    onCheckedChange = { isAiGenerated = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF7C3AED)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PostActionButton(
                    icon = Icons.Default.Image,
                    text = "Add Photo/Video",
                    onClick = { /* Handle media selection */ }
                )
                
                PostActionButton(
                    icon = Icons.Default.MovieFilter,
                    text = "Add Project",
                    onClick = { /* Handle project selection */ }
                )
                
                PostActionButton(
                    icon = Icons.Default.Person,
                    text = "Tag People",
                    onClick = { /* Handle people tagging */ }
                )
                
                PostActionButton(
                    icon = Icons.Default.LocationOn,
                    text = "Add Location",
                    onClick = { /* Handle location */ }
                )
            }
        }
    }
    
    if (isPosting) {
        // Show loading dialog
        AlertDialog(
            onDismissRequest = {},
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF7C3AED)
                    )
                    Text(
                        "Creating post...",
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun PostActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text,
                modifier = Modifier.padding(start = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Mock data generators
private fun generateMockComments(): List<Comment> {
    val currentTime = Clock.System.now()
    return listOf(
        Comment(
            id = "comment_1",
            author = FeedAuthor(
                id = "user_1",
                username = "user1",
                displayName = "Creative User",
                avatarUrl = null,
                isVerified = true
            ),
            text = "This is amazing! Love the AI integration 🔥",
            timestamp = currentTime - 30.minutes,
            likes = 24,
            hasLiked = true,
            replies = listOf(
                Comment(
                    id = "reply_1",
                    author = FeedAuthor(
                        id = "user_2",
                        username = "user2",
                        displayName = "Another User",
                        avatarUrl = null
                    ),
                    text = "Totally agree! The future is here",
                    timestamp = currentTime - 20.minutes,
                    likes = 5,
                    hasLiked = false
                )
            )
        ),
        Comment(
            id = "comment_2",
            author = FeedAuthor(
                id = "user_3",
                username = "filmmaker",
                displayName = "Pro Filmmaker",
                avatarUrl = null,
                isVerified = false
            ),
            text = "How did you achieve this effect? Tutorial please!",
            timestamp = currentTime - 2.hours,
            likes = 15,
            hasLiked = false
        )
    )
}

