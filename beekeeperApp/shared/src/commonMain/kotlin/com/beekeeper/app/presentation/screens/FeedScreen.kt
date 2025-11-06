// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/FeedScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.presentation.components.PrimaryTopBar
import com.beekeeper.app.presentation.theme.ThemeManager
import com.beekeeper.app.presentation.viewmodels.FeedEvent
import kotlinx.datetime.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.days
import com.beekeeper.app.presentation.viewmodels.FeedViewModel
import com.beekeeper.app.presentation.viewmodels.feed
import com.beekeeper.app.utils.formatDuration
import com.beekeeper.app.utils.formatTimestamp
import com.beekeeper.app.utils.formatCount


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onNavigateToProfile: (String) -> Unit,
    onNavigateToProject: (String) -> Unit,
    onNavigateToComments: (String) -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    viewModel: FeedViewModel = feed.rememberFeedViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(FeedTab.FOR_YOU) }
    var showNewPostSheet by remember { mutableStateOf(false) }
    val theme by ThemeManager.currentTheme.collectAsState()

    Scaffold(
        topBar = {
            PrimaryTopBar(
                title = "Feed",
                onNotificationClick = onNavigateToNotifications,
                actions = {
                    IconButton(onClick = onNavigateToCamera) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Create Post",
                            tint = theme.colors.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewPostSheet = true },
                icon = { Icon(Icons.Default.Add, "New Post") },
                text = { Text("Create") },
                containerColor = Color(0xFF7C3AED)
            )
        },
        containerColor = theme.colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            FeedTabRow(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            // Feed Content
            when (selectedTab) {
                FeedTab.FOR_YOU -> {
                    ForYouFeed(
                        onNavigateToProfile = onNavigateToProfile,
                        onNavigateToProject = onNavigateToProject,
                        onNavigateToComments = onNavigateToComments,
                        viewModel = viewModel,
                        posts = uiState.forYouPosts
                    )
                }
                FeedTab.FOLLOWING -> {
                    ForYouFeed(
                        onNavigateToProfile = onNavigateToProfile,
                        onNavigateToProject = onNavigateToProject,
                        onNavigateToComments = onNavigateToComments,
                        viewModel = viewModel,
                        posts = uiState.followingPosts
                    )
                }
                FeedTab.TRENDING -> {
                    ForYouFeed(
                        onNavigateToProfile = onNavigateToProfile,
                        onNavigateToProject = onNavigateToProject,
                        onNavigateToComments = onNavigateToComments,
                        viewModel = viewModel,
                        posts = uiState.trendingPosts
                    )
                }
            }
        }
    }

    // New Post Bottom Sheet
    if (showNewPostSheet) {
        NewPostBottomSheet(
            onDismiss = { showNewPostSheet = false },
            onPostCreated = { showNewPostSheet = false }
        )
    }
}

enum class FeedTab(val title: String) {
    FOR_YOU("For You"),
    FOLLOWING("Following"),
    TRENDING("Trending")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedTopBar(
    onNavigateToCamera: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.MovieFilter,
                    contentDescription = null,
                    tint = Color(0xFF7C3AED),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "CineFiller",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            }
        },
        actions = {
            IconButton(onClick = onNavigateToCamera) {
                Icon(Icons.Outlined.CameraAlt, "Camera")
            }
            IconButton(onClick = onNavigateToNotifications) {
                Badge(
                    containerColor = Color.Red,
                    modifier = Modifier.offset(x = (-8).dp, y = 8.dp)
                ) {
                    Text("3", fontSize = 10.sp)
                }
                Icon(Icons.Outlined.Notifications, "Notifications")
            }
        }
    )
}

@Composable
private fun FeedTabRow(
    selectedTab: FeedTab,
    onTabSelected: (FeedTab) -> Unit
) {
    TabRow(
        selectedTabIndex = FeedTab.values().indexOf(selectedTab),
        containerColor = Color.Transparent,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier.tabIndicatorOffset(
                    tabPositions[FeedTab.values().indexOf(selectedTab)]
                ),
                color = Color(0xFF7C3AED)
            )
        }
    ) {
        FeedTab.values().forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        tab.title,
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

@Composable
private fun ForYouFeed(
    onNavigateToProfile: (String) -> Unit,
    onNavigateToProject: (String) -> Unit,
    onNavigateToComments: (String) -> Unit,
    viewModel: FeedViewModel,
    posts: List<com.beekeeper.app.domain.model.FeedPost>
) {
    val theme = com.beekeeper.app.presentation.theme.rememberAppTheme()
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(posts) { post ->
            FeedPostCard(
                post = post,
                onProfileClick = { onNavigateToProfile(post.author.id) },
                onProjectClick = { post.content.projectReference?.let { onNavigateToProject(it.projectId) } },
                onCommentClick = { onNavigateToComments(post.id) },
                onLikeClick = {
                    if (post.hasBeenLiked) {
                        viewModel.handleEvent(FeedEvent.UnlikePost(post.id))
                    } else {
                        viewModel.handleEvent(FeedEvent.LikePost(post.id))
                    }
                },
                onShareClick = { viewModel.handleEvent(FeedEvent.SharePost(post.id)) },
                onBookmarkClick = {
                    if (post.hasBeenBookmarked) {
                        viewModel.handleEvent(FeedEvent.UnbookmarkPost(post.id))
                    } else {
                        viewModel.handleEvent(FeedEvent.BookmarkPost(post.id))
                    }
                },
                onMoreClick = { /* Handle more options */ }
            )

            // Divider between posts
            Divider(
                color = theme.colors.surfaceVariant,
                thickness = 0.5.dp
            )
        }
    }
}

@Composable
private fun FeedPostCard(
    post: FeedPost,
    onProfileClick: () -> Unit,
    onProjectClick: () -> Unit,
    onCommentClick: () -> Unit,
    onLikeClick: () -> Unit,
    onShareClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    val theme = com.beekeeper.app.presentation.theme.rememberAppTheme()
    var isLiked by remember { mutableStateOf(post.hasBeenLiked) }
    var isBookmarked by remember { mutableStateOf(post.hasBeenBookmarked) }
    var likeCount by remember { mutableStateOf(post.stats.likes) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.colors.surface)
            .padding(vertical = 12.dp)
    ) {
        // Author Header
        PostAuthorHeader(
            author = post.author,
            timestamp = post.timestamp,
            isSponsored = post.isSponsored,
            onProfileClick = onProfileClick,
            onMoreClick = onMoreClick
        )

        // Content
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clickable { onCommentClick() }
        ) {
            // Text Content
            post.content.text?.let { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Project Reference
            post.content.projectReference?.let { project ->
                ProjectReferenceCard(
                    project = project,
                    onClick = onProjectClick,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        // Media Content (outside padding for edge-to-edge)
        if (post.content.media.isNotEmpty()) {
            MediaCarousel(
                media = post.content.media,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // AI Generated Badge
        if (post.content.aiGenerated) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF7C3AED).copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF7C3AED),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "AI Generated",
                    fontSize = 12.sp,
                    color = Color(0xFF7C3AED)
                )
            }
        }

        // Interaction Bar
        PostInteractionBar(
            stats = post.stats,
            isLiked = isLiked,
            isBookmarked = isBookmarked,
            likeCount = likeCount,
            onLikeClick = {
                isLiked = !isLiked
                likeCount = if (isLiked) likeCount + 1 else likeCount - 1
                onLikeClick()
            },
            onCommentClick = onCommentClick,
            onShareClick = onShareClick,
            onBookmarkClick = {
                isBookmarked = !isBookmarked
                onBookmarkClick()
            }
        )
    }
}

@Composable
private fun PostAuthorHeader(
    author: FeedAuthor,
    timestamp: Instant,
    isSponsored: Boolean,
    onProfileClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    val theme = com.beekeeper.app.presentation.theme.rememberAppTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProfileClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        ) {
            // AsyncImage for actual avatar
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.align(Alignment.Center),
                tint = Color.White
            )
        }

        // Author info
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    author.displayName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                if (author.isVerified) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = Color(0xFF1DA1F2),
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(16.dp)
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "@${author.username}",
                    fontSize = 14.sp,
                    color = theme.colors.onSurfaceVariant
                )
                Text(
                    " · ",
                    fontSize = 14.sp,
                    color = theme.colors.onSurfaceVariant
                )
                Text(
                    formatTimestamp(timestamp),
                    fontSize = 14.sp,
                    color = theme.colors.onSurfaceVariant
                )
                if (isSponsored) {
                    Text(
                        " · Sponsored",
                        fontSize = 14.sp,
                        color = theme.colors.onSurfaceVariant
                    )
                }
            }
        }

        // Follow button (if not following)
        if (!author.isFollowing) {
            TextButton(onClick = { /* Handle follow */ }) {
                Text("Follow", color = Color(0xFF7C3AED))
            }
        }

        // More button
        IconButton(onClick = onMoreClick) {
            Icon(
                Icons.Default.MoreHoriz,
                contentDescription = "More options",
                tint = theme.colors.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProjectReferenceCard(
    project: ProjectReference,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = com.beekeeper.app.presentation.theme.rememberAppTheme()
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = theme.colors.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.MovieFilter,
                contentDescription = null,
                tint = Color(0xFF7C3AED),
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    project.projectName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    project.projectType,
                    fontSize = 12.sp,
                    color = theme.colors.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = theme.colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MediaCarousel(
    media: List<MediaItem>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        when {
            media.size == 1 -> {
                // Single media item
                SingleMediaView(media.first())
            }
            media.size > 1 -> {
                // Multiple media items - carousel
                var currentPage by remember { mutableStateOf(0) }

                Box {
                    // Pager for media items
                    MediaPager(
                        media = media,
                        currentPage = currentPage,
                        onPageChanged = { currentPage = it }
                    )

                    // Page indicator
                    if (media.size > 1) {
                        PageIndicator(
                            pageCount = media.size,
                            currentPage = currentPage,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SingleMediaView(media: MediaItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(media.aspectRatio)
            .background(Color.Black)
    ) {
        when (media.type) {
            MediaType.IMAGE, MediaType.AVATAR, MediaType.SCENE -> {
                // AsyncImage placeholder
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                }
            }
            MediaType.VIDEO -> {
                // Video player placeholder
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.White
                    )

                    // Duration badge
                    media.duration?.let { duration ->
                        Text(
                            formatDuration(duration),
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.7f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaPager(
    media: List<MediaItem>,
    currentPage: Int,
    onPageChanged: (Int) -> Unit
) {
    // Simplified pager implementation
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(media.first().aspectRatio)
    ) {
        SingleMediaView(media[currentPage])
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == currentPage) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == currentPage) Color.White
                        else Color.White.copy(alpha = 0.5f)
                    )
            )
        }
    }
}

@Composable
private fun PostInteractionBar(
    stats: PostStats,
    isLiked: Boolean,
    isBookmarked: Boolean,
    likeCount: Int,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    val theme = com.beekeeper.app.presentation.theme.rememberAppTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Like, Comment, Share
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Like button
            InteractionButton(
                icon = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                count = likeCount,
                onClick = onLikeClick,
                tint = if (isLiked) Color.Red else theme.colors.onSurfaceVariant
            )

            // Comment button
            InteractionButton(
                icon = Icons.Outlined.ChatBubbleOutline,
                count = stats.comments,
                onClick = onCommentClick
            )

            // Share button
            InteractionButton(
                icon = Icons.Outlined.Share,
                count = stats.shares,
                onClick = onShareClick
            )
        }

        // Bookmark
        IconButton(onClick = onBookmarkClick) {
            Icon(
                if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = "Bookmark",
                tint = if (isBookmarked) Color(0xFF7C3AED) else theme.colors.onSurfaceVariant
            )
        }
    }

    // View count
    if (stats.views > 0) {
        Text(
            "${formatCount(stats.views)} views",
            fontSize = 12.sp,
            color = theme.colors.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun InteractionButton(
    icon: ImageVector,
    count: Int,
    onClick: () -> Unit,
    tint: Color = com.beekeeper.app.presentation.theme.rememberAppTheme().colors.onSurfaceVariant
) {
    val theme = com.beekeeper.app.presentation.theme.rememberAppTheme()
    Row(
        modifier = Modifier.clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = tint
        )
        if (count > 0) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                formatCount(count),
                fontSize = 13.sp,
                color = theme.colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PostOption(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    val theme = com.beekeeper.app.presentation.theme.rememberAppTheme()
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = theme.colors.surfaceVariant
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
                tint = Color(0xFF7C3AED),
                modifier = Modifier.size(32.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    title,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    description,
                    fontSize = 14.sp,
                    color = theme.colors.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = theme.colors.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewPostBottomSheet(
    onDismiss: () -> Unit,
    onPostCreated: () -> Unit
) {
    val theme = com.beekeeper.app.presentation.theme.rememberAppTheme()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = theme.colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            Text(
                "Create New Post",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Post options
            PostOption(
                icon = Icons.Default.TextFields,
                title = "Text Post",
                description = "Share your thoughts",
                onClick = { /* Handle text post */ }
            )

            PostOption(
                icon = Icons.Default.Image,
                title = "Image/Video",
                description = "Upload from gallery",
                onClick = { /* Handle media post */ }
            )

            PostOption(
                icon = Icons.Default.AutoAwesome,
                title = "AI Generated",
                description = "Create with AI",
                onClick = { /* Handle AI post */ }
            )

            PostOption(
                icon = Icons.Default.MovieFilter,
                title = "Share Project",
                description = "Share from your projects",
                onClick = { /* Handle project share */ }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
