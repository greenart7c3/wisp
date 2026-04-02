package com.wisp.app.ui.component

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color as AndroidColor
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup.LayoutParams
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.common.util.UnstableApi
import com.wisp.app.R
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
    onExpandToFullScreen: (url: String, positionMs: Long) -> Unit
) {
    val state by PipController.pipState.collectAsState()
    val currentState = state ?: return
    val context = LocalContext.current

    DisposableEffect(currentState.url) {
        val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
        val density = activity.resources.displayMetrics.density
        val pipWidthPx = (200 * density).toInt()
        val pipHeightPx = ((200f / currentState.aspectRatio).coerceAtMost(260f) * density).toInt()
        val marginPx = (12 * density).toInt()
        val bottomMarginPx = (72 * density).toInt()
        val buttonSize = (28 * density).toInt()
        val buttonPadding = (4 * density).toInt()

        val dialog = Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar)
        dialog.setCancelable(false)

        // Track drag position
        var lastTouchX = 0f
        var lastTouchY = 0f
        var isDragging = false

        val root = FrameLayout(activity).apply {
            setOnTouchListener { _, event ->
                val params = dialog.window?.attributes ?: return@setOnTouchListener false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastTouchX = event.rawX
                        lastTouchY = event.rawY
                        isDragging = false
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX - lastTouchX
                        val dy = event.rawY - lastTouchY
                        if (!isDragging && (dx * dx + dy * dy) > (10 * density) * (10 * density)) {
                            isDragging = true
                        }
                        if (isDragging) {
                            params.x += dx.toInt()
                            params.y += dy.toInt()
                            dialog.window?.attributes = params
                            lastTouchX = event.rawX
                            lastTouchY = event.rawY
                        }
                        true
                    }
                    else -> false
                }
            }
        }

        val playerView = PlayerView(activity).apply {
            player = currentState.player
            useController = false
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
        root.addView(playerView)

        // Close button (top-end)
        val closeButton = ImageButton(activity).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            contentDescription = "Close"
            setBackgroundColor(0x99000000.toInt())
            setColorFilter(0xFFFFFFFF.toInt())
            layoutParams = FrameLayout.LayoutParams(buttonSize, buttonSize, Gravity.TOP or Gravity.END).apply {
                setMargins(buttonPadding, buttonPadding, buttonPadding, buttonPadding)
            }
            setOnClickListener { PipController.exitPip() }
        }
        root.addView(closeButton)

        // Expand button (top-start)
        val expandButton = ImageButton(activity).apply {
            setImageResource(R.drawable.ic_fullscreen)
            contentDescription = "Expand"
            setBackgroundColor(0x99000000.toInt())
            setColorFilter(0xFFFFFFFF.toInt())
            layoutParams = FrameLayout.LayoutParams(buttonSize, buttonSize, Gravity.TOP or Gravity.START).apply {
                setMargins(buttonPadding, buttonPadding, buttonPadding, buttonPadding)
            }
            setOnClickListener {
                val position = currentState.player.currentPosition
                val url = currentState.url
                PipController.pipState.value = null
                onExpandToFullScreen(url, position)
            }
        }
        root.addView(expandButton)

        dialog.setContentView(root, FrameLayout.LayoutParams(pipWidthPx, pipHeightPx))

        dialog.window?.let { window ->
            window.setLayout(pipWidthPx, pipHeightPx)
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setDimAmount(0f)
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            val displayMetrics = activity.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels
            window.attributes = window.attributes.apply {
                gravity = Gravity.TOP or Gravity.START
                x = screenWidth - pipWidthPx - marginPx
                y = screenHeight - pipHeightPx - bottomMarginPx
                flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            }
        }

        dialog.show()

        // Sync volume
        val player = currentState.player
        player.volume = if (PipController.globalMuted.value) 0f else 1f

        onDispose {
            if (dialog.isShowing) dialog.dismiss()
            val current = PipController.pipState.value
            if (current?.url == currentState.url) {
                PipController.exitPip()
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
