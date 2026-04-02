package com.wisp.app.ui.component

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.roundToInt

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
    onExpandToFullScreen: (url: String, positionMs: Long) -> Unit
) {
    val state by PipController.pipState.collectAsState()
    val currentState = state ?: return

    val isMuted by PipController.globalMuted.collectAsState()

    LaunchedEffect(isMuted) {
        currentState.player.volume = if (isMuted) 0f else 1f
    }

    DisposableEffect(currentState.url) {
        onDispose {
            val current = PipController.pipState.value
            if (current?.url == currentState.url) {
                PipController.exitPip()
            }
        }
    }

    val pipWidthDp = 200.dp
    val pipHeightDp = (200f / currentState.aspectRatio).dp.coerceAtMost(260.dp)

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val pipWidthPx = with(density) { pipWidthDp.toPx() }
    val pipHeightPx = with(density) { pipHeightDp.toPx() }
    val paddingPx = with(density) { 12.dp.toPx() }
    val bottomPaddingPx = with(density) { 72.dp.toPx() }

    // Start at bottom-end
    var offsetX by remember { mutableFloatStateOf(screenWidthPx - pipWidthPx - paddingPx) }
    var offsetY by remember { mutableFloatStateOf(screenHeightPx - pipHeightPx - bottomPaddingPx) }

    Popup(
        properties = PopupProperties(
            focusable = false,
            clippingEnabled = false
        )
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .width(pipWidthDp)
                .height(pipHeightDp)
                .shadow(8.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount.x)
                            .coerceIn(0f, screenWidthPx - pipWidthPx)
                        offsetY = (offsetY + dragAmount.y)
                            .coerceIn(0f, screenHeightPx - pipHeightPx)
                    }
                }
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = currentState.player
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
                    val position = currentState.player.currentPosition
                    val url = currentState.url
                    PipController.pipState.value = null
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
