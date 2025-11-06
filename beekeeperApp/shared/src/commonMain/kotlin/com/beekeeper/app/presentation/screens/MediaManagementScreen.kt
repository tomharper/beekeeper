// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/ui/screens/MediaManagementScreen.kt
package com.beekeeper.app.ui.screens

import MediaType
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.presentation.components.CineFillerImage
import com.beekeeper.app.presentation.components.MediaGridImage
import com.beekeeper.app.presentation.components.VideoPlayerWithControls
import com.beekeeper.app.presentation.components.VideoThumbnail
import com.beekeeper.app.presentation.media.MediaUploadViewModel
import com.beekeeper.app.ui.components.*
import com.beekeeper.app.utils.FilePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import com.beekeeper.app.domain.model.*

/**
 * Complete media management screen showing upload, display, and video playback
 * @param filePicker Platform-specific file picker instance (optional for preview)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaManagementScreen(
    projectId: String,
    filePicker: FilePicker? = null,
    onNavigateBack: () -> Unit = {}
) {
    val uploadViewModel: MediaUploadViewModel = viewModel()
    val uploadState by uploadViewModel.state.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Images", "Videos", "Audio")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Media Library") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (filePicker != null) {
                        MediaUploadButton(
                            projectId = projectId,
                            filePicker = filePicker,
                            onUploadComplete = { asset ->
                                // Handle upload completion
                            }
                        )
                    } else {
                        MediaUploadButtonPreview(projectId = projectId)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Error message
            uploadState.error?.let { error ->
                UploadErrorMessage(
                    error = error,
                    onDismiss = { uploadViewModel.clearError() },
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Upload queue
            if (uploadState.uploads.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Active Uploads",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        UploadQueue()
                    }
                }
            }

            // Tab selector
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Content based on selected tab
            when (selectedTab) {
                0 -> AllMediaGrid(projectId)
                1 -> ImageGallery(projectId)
                2 -> VideoGallery(projectId)
                3 -> AudioList(projectId)
            }
        }
    }
}

/**
 * Grid showing all media types
 */
@Composable
private fun AllMediaGrid(projectId: String) {
    // Sample media items - in real app, load from repository
    val mediaItems = remember { generateSampleMedia() }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(120.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(mediaItems.size) { index ->
            MediaGridItem(
                item = mediaItems[index],
                onClick = { /* Handle click */ }
            )
        }
    }
}

/**
 * Image gallery view
 */
@Composable
private fun ImageGallery(projectId: String) {
    val images = remember {
        listOf(
            "https://picsum.photos/400/300?random=1",
            "https://picsum.photos/400/300?random=2",
            "https://picsum.photos/400/300?random=3",
            "https://picsum.photos/400/300?random=4",
            "https://picsum.photos/400/300?random=5",
            "https://picsum.photos/400/300?random=6"
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(images.size) { index ->
            MediaGridImage(
                url = images[index],
                contentDescription = "Image ${index + 1}",
                aspectRatio = 4f / 3f,
                onClick = { /* Open full screen viewer */ }
            )
        }
    }
}

/**
 * Video gallery with thumbnails
 */
@Composable
private fun VideoGallery(projectId: String) {
    val videos = remember { generateSampleVideos() }
    var selectedVideo by remember { mutableStateOf<VideoData?>(null) }

    if (selectedVideo != null) {
        // Video player dialog
        AlertDialog(
            onDismissRequest = { selectedVideo = null },
            title = { Text(selectedVideo?.title ?: "Video") },
            text = {
                VideoPlayerWithControls(
                    url = selectedVideo?.url ?: "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    autoPlay = true
                )
            },
            confirmButton = {
                TextButton(onClick = { selectedVideo = null }) {
                    Text("Close")
                }
            }
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(200.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(videos.size) { index ->
            VideoThumbnail(
                thumbnailUrl = videos[index].thumbnailUrl,
                duration = videos[index].duration,
                onClick = { selectedVideo = videos[index] },
                modifier = Modifier.aspectRatio(16f / 9f)
            )
        }
    }
}

/**
 * Audio files list
 */
@Composable
private fun AudioList(projectId: String) {
    val audioFiles = remember { generateSampleAudio() }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(audioFiles.size) { index ->
            AudioListItem(
                audio = audioFiles[index],
                onClick = { /* Play audio */ }
            )
        }
    }
}

/**
 * Individual media grid item
 */
@Composable
private fun MediaGridItem(
    item: MediaItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            when (item.type) {
                MediaType.IMAGE -> {
                    CineFillerImage(
                        url = item.thumbnailUrl,
                        contentDescription = item.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    )
                }
                MediaType.VIDEO, MediaType.SHORT, MediaType.LIVE, MediaType.STORY, MediaType.REEL, MediaType.CAROUSEL -> {
                    VideoThumbnail(
                        thumbnailUrl = item.thumbnailUrl,
                        duration = item.duration,
                        onClick = onClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    )
                }
                MediaType.AUDIO -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.AudioFile,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                }
            }

            // Type badge
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Text(
                    item.type.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Audio list item
 */
@Composable
private fun AudioListItem(
    audio: AudioData,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Filled.MusicNote,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    audio.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${audio.artist} • ${formatDuration(audio.duration)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onClick) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Play")
            }
        }
    }
}

// Data classes for the sample
data class MediaItem(
    val id: String,
    val title: String,
    val type: MediaType,
    val thumbnailUrl: String,
    val url: String,
    val duration: Long? = null
)

data class VideoData(
    val id: String,
    val title: String,
    val url: String,
    val thumbnailUrl: String,
    val duration: Long
)

data class AudioData(
    val id: String,
    val title: String,
    val artist: String,
    val url: String,
    val duration: Long
)

// Sample data generators
private fun generateSampleMedia(): List<MediaItem> {
    return listOf(
        MediaItem("1", "Image 1", MediaType.IMAGE, "https://picsum.photos/200/200?random=1", ""),
        MediaItem("2", "Video 1", MediaType.VIDEO, "https://picsum.photos/200/200?random=2", "", 120000),
        MediaItem("3", "Audio 1", MediaType.AUDIO, "", "", 180000),
        MediaItem("4", "Image 2", MediaType.IMAGE, "https://picsum.photos/200/200?random=3", ""),
        MediaItem("5", "Video 2", MediaType.VIDEO, "https://picsum.photos/200/200?random=4", "", 90000),
        MediaItem("6", "Image 3", MediaType.IMAGE, "https://picsum.photos/200/200?random=5", "")
    )
}

private fun generateSampleVideos(): List<VideoData> {
    return listOf(
        VideoData("v1", "Sample Video 1", "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4", "https://picsum.photos/320/180?random=1", 60000),
        VideoData("v2", "Sample Video 2", "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4", "https://picsum.photos/320/180?random=2", 90000),
        VideoData("v3", "Sample Video 3", "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4", "https://picsum.photos/320/180?random=3", 120000)
    )
}

private fun generateSampleAudio(): List<AudioData> {
    return listOf(
        AudioData("a1", "Background Music 1", "CineFiller", "https://example.com/audio1.mp3", 180000),
        AudioData("a2", "Sound Effect 1", "CineFiller", "https://example.com/audio2.mp3", 5000),
        AudioData("a3", "Narration Track", "Voice Artist", "https://example.com/audio3.mp3", 240000)
    )
}

private fun formatDuration(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "$minutes:${remainingSeconds.toString().padStart(2, '0')}"
}

