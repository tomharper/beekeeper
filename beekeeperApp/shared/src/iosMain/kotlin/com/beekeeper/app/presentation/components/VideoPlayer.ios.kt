// -----------------------------------------------------------
// File: shared/src/iosMain/kotlin/com/cinefiller/fillerapp/presentation/components/VideoPlayer.ios.kt
package com.beekeeper.app.presentation.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.AVFoundation.*
import platform.AVKit.AVPlayerViewController
import platform.CoreMedia.*
import platform.Foundation.*
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIView
import platform.darwin.NSObject
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoPlayer(
    url: String,
    modifier: Modifier,
    isPlaying: Boolean,
    showControls: Boolean,
    autoPlay: Boolean,
    onStateChange: (VideoPlayerState) -> Unit
) {
    val player = remember {
        AVPlayer(uRL = NSURL.URLWithString(url)!!).apply {
            if (autoPlay) {
                play()
            }
        }
    }
    
    // Update playing state
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            player.play()
        } else {
            player.pause()
        }
    }
    
    // Observe player state
    DisposableEffect(player) {
        val observer = PlayerObserver(player, onStateChange)
        
        onDispose {
            observer.cleanup()
            player.pause()
        }
    }
    
    // Update position periodically
    LaunchedEffect(player) {
        while (isActive) {
            val currentTime = player.currentTime()
            val duration = player.currentItem?.duration ?: CMTimeMake(0, 1)
            
            if (currentTime.isValid() && duration.isValid()) {
                onStateChange(
                    VideoPlayerState(
                        isPlaying = player.rate > 0,
                        isLoading = player.status == AVPlayerStatusUnknown,
                        duration = (CMTimeGetSeconds(duration) * 1000).toLong(),
                        currentPosition = (CMTimeGetSeconds(currentTime) * 1000).toLong()
                    )
                )
            }
        }
    }
    
    UIKitView(
        factory = {
            val playerViewController = AVPlayerViewController().apply {
                this.player = player
                showsPlaybackControls = showControls
            }
            playerViewController.view
        },
        modifier = modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalForeignApi::class)
private class PlayerObserver(
    private val player: AVPlayer,
    private val onStateChange: (VideoPlayerState) -> Unit
) : NSObject() {

    init {
        // Observe player status
        player.addObserver(
            observer = this,
            forKeyPath = "status",
            options = NSKeyValueObservingOptionNew,
            context = null
        )

        // Observe player item changes
        NSNotificationCenter.defaultCenter.addObserver(
            observer = this,
            selector = NSSelectorFromString("playerItemDidPlayToEnd:"),
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = player.currentItem
        )
    }
    
    @Suppress("UNUSED_PARAMETER")
    fun playerItemDidPlayToEnd(notification: NSNotification) {
        onStateChange(
            VideoPlayerState(
                isPlaying = false,
                isLoading = false,
                error = null
            )
        )
    }
    
    fun cleanup() {
        player.removeObserver(this, forKeyPath = "status")
        NSNotificationCenter.defaultCenter.removeObserver(this)
    }
}

// Extension functions for CMTime
@OptIn(ExperimentalForeignApi::class)
private fun CValue<CMTime>.isValid(): Boolean {
    return this.useContents {
        (this.flags.toInt() and kCMTimeFlags_Valid.toInt()) != 0
    }
}
