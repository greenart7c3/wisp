package com.wisp.app.ui.component

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.flow.MutableStateFlow

data class PipState(
    val url: String,
    val player: ExoPlayer,
    val aspectRatio: Float
)

object PipController {
    val globalMuted = MutableStateFlow(true)
    val activeVideoUrl = MutableStateFlow<String?>(null)
    val pipState = MutableStateFlow<PipState?>(null)

    fun enterPip(url: String, player: ExoPlayer, aspectRatio: Float) {
        val old = pipState.value
        if (old != null && old.url != url) {
            old.player.release()
        }
        pipState.value = PipState(url, player, aspectRatio)
        activeVideoUrl.value = url
    }

    fun exitPip() {
        val state = pipState.value ?: return
        state.player.release()
        pipState.value = null
        activeVideoUrl.compareAndSet(state.url, null)
    }

    fun reclaimPlayer(url: String): ExoPlayer? {
        val state = pipState.value ?: return null
        if (state.url != url) return null
        pipState.value = null
        return state.player
    }
}

@OptIn(UnstableApi::class)
@Composable
fun FloatingVideoPlayer(
    onExpandToFullScreen: (url: String, positionMs: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by PipController.pipState.collectAsState()
    val currentState = state

    AnimatedVisibility(
        visible = currentState != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        val pipState = currentState ?: return@AnimatedVisibility
        val isMuted by PipController.globalMuted.collectAsState()

        LaunchedEffect(isMuted) {
            pipState.player.volume = if (isMuted) 0f else 1f
        }

        DisposableEffect(pipState.url) {
            onDispose {
                val current = PipController.pipState.value
                if (current?.url == pipState.url) {
                    PipController.exitPip()
                }
            }
        }

        val pipWidth = 200.dp
        val pipHeight = (200f / pipState.aspectRatio).dp.coerceAtMost(260.dp)

        Box(
            modifier = Modifier
                .width(pipWidth)
                .height(pipHeight)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = pipState.player
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            val buttonColors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Black.copy(alpha = 0.6f),
                contentColor = Color.White
            )

            IconButton(
                onClick = { PipController.exitPip() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(28.dp),
                colors = buttonColors
            ) {
                Icon(Icons.Filled.Close, "Close", Modifier.size(16.dp))
            }

            IconButton(
                onClick = {
                    val position = pipState.player.currentPosition
                    val url = pipState.url
                    // Try to reclaim inline first; if not possible, go fullscreen
                    PipController.pipState.value = null
                    // Give inline a chance to reclaim, otherwise expand to fullscreen
                    onExpandToFullScreen(url, position)
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .size(28.dp),
                colors = buttonColors
            ) {
                Icon(Icons.Filled.Fullscreen, "Expand", Modifier.size(16.dp))
            }
        }
    }
}
