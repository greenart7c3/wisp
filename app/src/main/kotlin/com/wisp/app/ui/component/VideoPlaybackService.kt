package com.wisp.app.ui.component

import android.content.Intent
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class VideoPlaybackService : MediaSessionService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        VideoMediaSession.session?.let { addSession(it) }
        return START_NOT_STICKY
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return VideoMediaSession.session
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val session = VideoMediaSession.session
        if (session == null || !session.player.playWhenReady) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        VideoMediaSession.session?.let { removeSession(it) }
        super.onDestroy()
    }
}
