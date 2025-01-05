package com.example.mustag.player.service

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import com.example.mustag.player.notification.JetAudioNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class JetAudioService : MediaSessionService() {
    @Inject
    lateinit var mediaSession: MediaSession

    @Inject
    lateinit var notificationManager: JetAudioNotificationManager

    @Inject
    lateinit var jetAudioServiceHandler: JetAudioServiceHandler

    @UnstableApi
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.startNotificationService(
                mediaSession = mediaSession,
                mediaSessionService = this
            )
        }

        jetAudioServiceHandler.restorePlayerState()


        return super.onStartCommand(intent, flags, startId)
    }


    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession =
        mediaSession

    @SuppressLint("MissingSuperCall")
    override fun onDestroy() {
        // Сохраняем состояние плеера при его остановке
        jetAudioServiceHandler.savePlayerState()

        mediaSession.apply {
            release()
            if (player.playbackState != Player.STATE_IDLE) {
                player.seekTo(0)
                player.playWhenReady = false
                player.stop()
            }
        }
        super.onDestroy()

    }
}
data class PlayerState(
    val currentMediaItemIndex: Int,
    val playbackPosition: Long,
    val isPlaying: Boolean
)
