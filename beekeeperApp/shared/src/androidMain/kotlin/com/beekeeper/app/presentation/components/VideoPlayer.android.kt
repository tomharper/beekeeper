// -----------------------------------------------------------
// File: shared/src/androidMain/kotlin/com/cinefiller/fillerapp/ui/components/VideoPlayer.kt
package com.beekeeper.app.presentation.components

import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.isActive

@Composable
actual fun VideoPlayer(
    url: String,
    modifier: Modifier,
    isPlaying: Boolean,
    showControls: Boolean,
    autoPlay: Boolean,
    onStateChange: (VideoPlayerState) -> Unit
) {
    val context = LocalContext.current
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = autoPlay
        }
    }
    
    // Update playing state
    LaunchedEffect(isPlaying) {
        exoPlayer.playWhenReady = isPlaying
    }
    
    // Track player state
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                onStateChange(
                    VideoPlayerState(
                        isPlaying = exoPlayer.isPlaying,
                        isLoading = playbackState == Player.STATE_BUFFERING,
                        duration = exoPlayer.duration.coerceAtLeast(0),
                        currentPosition = exoPlayer.currentPosition.coerceAtLeast(0),
                        error = if (playbackState == Player.STATE_ENDED) null else null
                    )
                )
            }
            
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                onStateChange(
                    VideoPlayerState(
                        isPlaying = isPlaying,
                        isLoading = false,
                        duration = exoPlayer.duration.coerceAtLeast(0),
                        currentPosition = exoPlayer.currentPosition.coerceAtLeast(0)
                    )
                )
            }
        }
        
        exoPlayer.addListener(listener)
        
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }
    
    // Update position periodically
    LaunchedEffect(exoPlayer) {
        while (isActive) {
            if (exoPlayer.isPlaying) {
                onStateChange(
                    VideoPlayerState(
                        isPlaying = true,
                        isLoading = false,
                        duration = exoPlayer.duration.coerceAtLeast(0),
                        currentPosition = exoPlayer.currentPosition.coerceAtLeast(0)
                    )
                )
            }
        }
    }
    
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = showControls
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier = modifier.fillMaxSize()
    )
}

