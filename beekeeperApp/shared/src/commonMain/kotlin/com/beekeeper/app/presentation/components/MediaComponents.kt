// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/components/MediaComponents.kt
package com.beekeeper.app.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import com.beekeeper.app.presentation.viewmodels.*

/**
 * Reusable media display component for images using Kamel
 */
@Composable
fun MediaImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null
) {
    KamelImage(
        resource = asyncPainterResource(imageUrl),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        onLoading = { progress ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = progress ?: 0f,
                    modifier = Modifier.size(48.dp),
                    color = Color(0xFF4A90E2)
                )
            }
        },
        onFailure = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF2A2A2A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.BrokenImage,
                    contentDescription = "Failed to load",
                    tint = Color.Gray,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    )
}


/**
 * Storyboard frame player component
 */
@Composable
fun StoryboardFramePlayer(
    viewModel: StoryboardMediaViewModel,
    modifier: Modifier = Modifier
) {
    val currentFrameIndex by viewModel.currentFrameIndex.collectAsState()
    val frames by viewModel.frames.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val autoPlayEnabled by viewModel.autoPlayEnabled.collectAsState()
    val currentMediaUrl by viewModel.currentMediaUrl.collectAsState()
    
    val currentFrame = frames.getOrNull(currentFrameIndex)
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
    ) {
        // Display current frame
        currentMediaUrl?.let { url ->
            when {
                currentFrame?.sceneData?.videoUrl != null -> {
                    VideoPlayer(
                        url = url,
                        isPlaying = isPlaying,
                        modifier = Modifier.fillMaxSize(),
                        onStateChange = {
                            if (autoPlayEnabled) {
                                viewModel.nextFrame()
                            }
                        }
                    )
                }
                currentFrame?.sceneData?.imageUrl != null -> {
                    MediaImage(
                        imageUrl = url,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
        
        // Controls overlay
        StoryboardControls(
            currentFrameIndex = currentFrameIndex,
            totalFrames = frames.size,
            isPlaying = autoPlayEnabled,
            onPlayPause = {
                if (autoPlayEnabled) {
                    viewModel.stopAutoPlay()
                } else {
                    viewModel.startAutoPlay()
                }
            },
            onPrevious = { viewModel.previousFrame() },
            onNext = { viewModel.nextFrame() },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Avatar video player component
 */
@Composable
fun AvatarVideoPlayer(
    viewModel: AvatarMediaViewModel,
    modifier: Modifier = Modifier
) {
    val currentMediaUrl by viewModel.currentMediaUrl.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val error by viewModel.error.collectAsState()
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f), // Portrait for avatars
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                isGenerating -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = Color(0xFF4A90E2)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Generating avatar...",
                            color = Color.White
                        )
                    }
                }
                error != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            error ?: "Error loading avatar",
                            color = Color.White
                        )
                    }
                }
                currentMediaUrl != null -> {
                    VideoPlayer(
                        url = currentMediaUrl!!,
                        isPlaying = isPlaying,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Simple play/pause overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { viewModel.togglePlayback() },
                        contentAlignment = Alignment.Center
                    ) {
                        this@Card.AnimatedVisibility(
                            visible = !isPlaying,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Icon(
                                Icons.Default.PlayCircle,
                                contentDescription = "Play",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }
                }
                else -> {
                    // Empty state
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "No avatar",
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No avatar loaded",
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

/**
 * Video editor component with timeline
 */
@Composable
fun VideoEditorPlayer(
    viewModel: VideoEditorViewModel,
    modifier: Modifier = Modifier
) {
    val currentMediaUrl by viewModel.currentMediaUrl.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val volume by viewModel.volume.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    
    Column(modifier = modifier.fillMaxSize()) {
        // Video preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black)
        ) {
            currentMediaUrl?.let { url ->
                VideoPlayer(
                    url = url,
                    isPlaying = isPlaying,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Overlay controls
            VideoEditorOverlay(
                isPlaying = isPlaying,
                onPlayPause = { viewModel.togglePlayback() },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Timeline and controls
        VideoEditorControls(
            currentPosition = currentPosition,
            duration = duration,
            isPlaying = isPlaying,
            volume = volume,
            isMuted = isMuted,
            playbackSpeed = playbackSpeed,
            onSeek = { viewModel.seekTo(it) },
            onPlayPause = { viewModel.togglePlayback() },
            onVolumeChange = { viewModel.setVolume(it) },
            onMuteToggle = { viewModel.toggleMute() },
            onSpeedChange = { viewModel.setPlaybackSpeed(it) }
        )
    }
}

/**
 * Storyboard controls overlay
 */
@Composable
private fun StoryboardControls(
    currentFrameIndex: Int,
    totalFrames: Int,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Frame indicator
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(
                    Color.Black.copy(alpha = 0.7f),
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                "Frame ${currentFrameIndex + 1} / $totalFrames",
                color = Color.White
            )
        }
        
        // Playback controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = onPrevious,
                enabled = currentFrameIndex > 0
            ) {
                Icon(
                    Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = if (currentFrameIndex > 0) Color.White else Color.Gray
                )
            }
            
            FloatingActionButton(
                onClick = onPlayPause,
                containerColor = Color(0xFF4A90E2)
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White
                )
            }
            
            IconButton(
                onClick = onNext,
                enabled = currentFrameIndex < totalFrames - 1
            ) {
                Icon(
                    Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = if (currentFrameIndex < totalFrames - 1) Color.White else Color.Gray
                )
            }
        }
    }
}

/**
 * Video editor overlay
 */
@Composable
private fun VideoEditorOverlay(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.clickable { onPlayPause() },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = !isPlaying,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

/**
 * Video editor controls
 */
@Composable
private fun VideoEditorControls(
    currentPosition: Long,
    duration: Long,
    isPlaying: Boolean,
    volume: Float,
    isMuted: Boolean,
    playbackSpeed: Float,
    onSeek: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onMuteToggle: () -> Unit,
    onSpeedChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Timeline
            Slider(
                value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                onValueChange = { onSeek((it * duration).toLong()) },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF4A90E2),
                    activeTrackColor = Color(0xFF4A90E2),
                    inactiveTrackColor = Color(0xFF4A4A4A)
                )
            )
            
            // Controls row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play/Pause
                IconButton(onClick = onPlayPause) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White
                    )
                }
                
                // Time display
                Text(
                    "${formatTime(currentPosition)} / ${formatTime(duration)}",
                    color = Color.White
                )
                
                // Volume
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onMuteToggle) {
                        Icon(
                            if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Volume",
                            tint = Color.White
                        )
                    }
                    Slider(
                        value = volume,
                        onValueChange = onVolumeChange,
                        modifier = Modifier.width(100.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color(0xFF4A4A4A)
                        )
                    )
                }
                
                // Speed
                TextButton(onClick = { 
                    val nextSpeed = when (playbackSpeed) {
                        0.5f -> 1f
                        1f -> 1.5f
                        1.5f -> 2f
                        else -> 0.5f
                    }
                    onSpeedChange(nextSpeed)
                }) {
                    Text("${playbackSpeed}x", color = Color.White)
                }
            }
        }
    }
}

// Helper function
private fun formatTime(millis: Long): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return when {
        hours > 0 -> "$hours:${(minutes % 60).toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}"
        else -> "$minutes:${(seconds % 60).toString().padStart(2, '0')}"
    }
}
