package com.example.mustag.player.service

import android.content.SharedPreferences
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

class JetAudioServiceHandler @Inject constructor(
    private val exoPlayer: ExoPlayer,
    private val sharedPreferences: SharedPreferences
) : Player.Listener {

    private val _audioState: MutableStateFlow<JetAudioState> = MutableStateFlow(JetAudioState.Initial)
    val audioState: StateFlow<JetAudioState> = _audioState.asStateFlow()

    private var progressJob: Job? = null
    private val coroutineScope = MainScope()


    init {
        exoPlayer.addListener(this)
    }

    fun addMediaItem(mediaItem: MediaItem) {
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    fun setMediaItemList(mediaItems: List<MediaItem>) {
        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()
    }

    suspend fun onPlayerEvents(
        playerEvent: PlayerEvent,
        selectedAudioIndex: Int = -1,
        seekPosition: Long = 0,
    ) {
        when (playerEvent) {
            PlayerEvent.Backward -> seekBack()
            PlayerEvent.Forward -> seekForward()
            PlayerEvent.SeekToNext -> seekToNext()
            PlayerEvent.SeekToPrevious -> seekToPrevious()
            PlayerEvent.PlayPause -> playOrPause()
            PlayerEvent.SeekTo -> seekTo(seekPosition)
            PlayerEvent.SelectedAudioChange -> setSelectedAudio(selectedAudioIndex)
            PlayerEvent.Stop -> stopPlayer()
            is PlayerEvent.UpdateProgress -> seekToPercentage(playerEvent.newProgress)
        }
    }

    private fun seekBack() {
        exoPlayer.seekBack()
    }

    private fun seekForward() {
        exoPlayer.seekForward()
    }

    private fun seekToNext() {
        exoPlayer.seekToNext()
    }

    private fun seekToPrevious() {
        exoPlayer.seekToPrevious()
    }

    private fun playOrPause() {
        if(exoPlayer.isPlaying) exoPlayer.pause()
        else exoPlayer.play()
    }

    private fun seekTo(seekPosition:Long) {
        exoPlayer.seekTo(seekPosition)
    }

    private fun seekToPercentage(percentage:Float){
        exoPlayer.seekTo((exoPlayer.duration * percentage).toLong())
    }

    private fun setSelectedAudio(selectedAudioIndex: Int) {
        if (selectedAudioIndex in 0 until exoPlayer.mediaItemCount) {
            if (selectedAudioIndex == exoPlayer.currentMediaItemIndex) {
                playOrPause()
            } else {
                exoPlayer.seekToDefaultPosition(selectedAudioIndex)
                exoPlayer.playWhenReady = true
            }
            // Устанавливаем состояние текущего элемента
            _audioState.value = JetAudioState.CurrentPlaying(selectedAudioIndex)
        } else {
            Log.e("AudioHandler", "Invalid selectedAudioIndex: $selectedAudioIndex")
        }
    }



    private fun stopPlayer() {
        stopProgressUpdate()
        exoPlayer.stop()
        exoPlayer.clearMediaItems()

    }
    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            ExoPlayer.STATE_BUFFERING ->  _audioState.value = JetAudioState.Buffering(exoPlayer.currentPosition)
            ExoPlayer.STATE_READY -> _audioState.value = JetAudioState.Ready(exoPlayer.duration)
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        val currentMediaItemIndex = exoPlayer.currentMediaItemIndex
        val currentMediaItem = exoPlayer.currentMediaItem
        _audioState.value = JetAudioState.Playing(
            isPlaying = isPlaying,
            currentMediaItemIndex = currentMediaItemIndex,
            currentMediaItem = currentMediaItem
        )

        if (isPlaying) {
            startProgressUpdate()
        } else {
            stopProgressUpdate()
        }
    }


    private fun startProgressUpdate() {
        if(progressJob?.isActive == true) return

        progressJob = coroutineScope.launch(Dispatchers.Main){
            while (exoPlayer.isPlaying) {
                _audioState.value = JetAudioState.Progress(exoPlayer.currentPosition)
                delay(500)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
    }

    fun savePlayerState() {
        val state =  PlayerState(
            exoPlayer.currentMediaItemIndex,
            exoPlayer.currentPosition,
            exoPlayer.isPlaying
        )

        val json = "{\"currentMediaItemIndex\":${state.currentMediaItemIndex}," +
                "\"playbackPosition\":${state.playbackPosition}," +
                "\"isPlaying\":${state.isPlaying}}"
        sharedPreferences.edit().putString("player_state", json).apply()

    }

    fun restorePlayerState() {
        val json = sharedPreferences.getString("player_state", null)
        if(json == null) return

        val state = json.let {
            val regex =  "\\{\"currentMediaItemIndex\":(\\d+),\"playbackPosition\":(\\d+),\"isPlaying\":(true|false)\\}".toRegex()

            val matchResult = regex.find(it)

            if (matchResult != null) {
                val (currentMediaItemIndex, playbackPosition, isPlaying) = matchResult.destructured
                PlayerState(
                    currentMediaItemIndex.toInt(),
                    playbackPosition.toLong(),
                    isPlaying.toBoolean()
                )
            } else null
        } ?: return

        if (exoPlayer.mediaItemCount == 0) return

        if(state.currentMediaItemIndex >= 0) exoPlayer.seekToDefaultPosition(state.currentMediaItemIndex)

        exoPlayer.seekTo(state.playbackPosition)
        exoPlayer.playWhenReady = state.isPlaying

    }

    fun isPlaying() = exoPlayer.isPlaying
    fun getDuration() = exoPlayer.duration
    fun getProgress() = exoPlayer.currentPosition

    fun getCurrentMediaItem(): MediaItem? {
        return exoPlayer.currentMediaItem
    }

}




sealed class PlayerEvent {
    object PlayPause : PlayerEvent()
    object SelectedAudioChange : PlayerEvent()
    object Backward : PlayerEvent()
    object SeekToNext : PlayerEvent()
    object SeekToPrevious : PlayerEvent()
    object Forward : PlayerEvent()
    object SeekTo : PlayerEvent()
    object Stop : PlayerEvent()
    data class UpdateProgress(val newProgress: Float) : PlayerEvent()
}

sealed class JetAudioState {
    object Initial : JetAudioState()
    data class Playing(
        val isPlaying: Boolean,
        val currentMediaItemIndex: Int,
        val currentMediaItem: MediaItem?
    ) : JetAudioState()
    data class Progress(val progress: Long) : JetAudioState()
    data class Buffering(val progress: Long) : JetAudioState()
    data class Ready(val duration: Long) : JetAudioState()
    data class CurrentPlaying(val mediaItemIndex: Int) : JetAudioState()
}



//class JetAudioServiceHandler @Inject constructor(
//    private val exoPlayer: ExoPlayer,
//    private val sharedPreferences: SharedPreferences
//) : Player.Listener {
//
//    private val _audioState: MutableStateFlow<JetAudioState> = MutableStateFlow(JetAudioState.Initial)
//    val audioState: StateFlow<JetAudioState> = _audioState.asStateFlow()
//
//    private var job: Job? = null
//
//    init {
//        exoPlayer.addListener(this)
//    }
//
//    fun addMediaItem(mediaItem: MediaItem) {
//        exoPlayer.setMediaItem(mediaItem)
//        exoPlayer.prepare()
//    }
//
//    fun setMediaItemList(mediaItems: List<MediaItem>) {
//        exoPlayer.setMediaItems(mediaItems)
//        exoPlayer.prepare()
//    }
//
//    suspend fun onPlayerEvents(
//        playerEvent: PlayerEvent,
//        selectedAudioIndex: Int = -1,
//        seekPosition: Long = 0,
//    ) {
//        when (playerEvent) {
//            PlayerEvent.Backward -> exoPlayer.seekBack()
//            PlayerEvent.Forward -> exoPlayer.seekForward()
//            PlayerEvent.SeekToNext -> exoPlayer.seekToNext()
//            PlayerEvent.PlayPause -> playOrPause()
//            PlayerEvent.SeekTo -> exoPlayer.seekTo(seekPosition)
//            PlayerEvent.SelectedAudioChange -> {
//                when (selectedAudioIndex) {
//                    exoPlayer.currentMediaItemIndex -> {
//                        playOrPause()
//                    }
//
//                    else -> {
//                        exoPlayer.seekToDefaultPosition(selectedAudioIndex)
//                        _audioState.value = JetAudioState.Playing(isPlaying = true)
//                        exoPlayer.playWhenReady = true
//                        startProgressUpdate()
//                    }
//                }
//            }
//
//            PlayerEvent.Stop -> stopProgressUpdate()
//            is PlayerEvent.UpdateProgress -> {
//                exoPlayer.seekTo((exoPlayer.duration * playerEvent.newProgress).toLong())
//            }
//        }
//    }
//
//    override fun onPlaybackStateChanged(playbackState: Int) {
//        when (playbackState) {
//            ExoPlayer.STATE_BUFFERING -> _audioState.value = JetAudioState.Buffering(exoPlayer.currentPosition)
//            ExoPlayer.STATE_READY -> _audioState.value = JetAudioState.Ready(exoPlayer.duration)
//        }
//    }
//
//    override fun onIsPlayingChanged(isPlaying: Boolean) {
//        _audioState.value = JetAudioState.Playing(isPlaying = isPlaying)
//        _audioState.value = JetAudioState.CurrentPlaying(exoPlayer.currentMediaItemIndex)
//
//        if (isPlaying) {
//            GlobalScope.launch(Dispatchers.Main) {
//                startProgressUpdate()
//            }
//        } else {
//            stopProgressUpdate()
//        }
//    }
//
//    private suspend fun playOrPause() {
//        if (exoPlayer.isPlaying) {
//            exoPlayer.pause()
//            stopProgressUpdate()
//        } else {
//            exoPlayer.play()
//            _audioState.value = JetAudioState.Playing(isPlaying = true)
//            startProgressUpdate()
//        }
//    }
//
//    private suspend fun startProgressUpdate() = job.run {
//        while (true) {
//            delay(500)
//            _audioState.value = JetAudioState.Progress(exoPlayer.currentPosition)
//        }
//    }
//
//    private fun stopProgressUpdate() {
//        job?.cancel()
//        _audioState.value = JetAudioState.Playing(isPlaying = false)
//    }
//
//    // Сохранение состояния
//    fun savePlayerState() {
//        val currentTrackIndex = exoPlayer.currentMediaItemIndex
//        val playbackPosition = exoPlayer.currentPosition
//        sharedPreferences.edit()
//            .putInt("current_track_index", currentTrackIndex)
//            .putLong("playback_position", playbackPosition)
//            .apply()
//    }
//
//    // Восстановление состояния
//    fun restorePlayerState() {
//        val currentTrackIndex = sharedPreferences.getInt("current_track_index", 0)
//        val playbackPosition = sharedPreferences.getLong("playback_position", 0L)
//
//        exoPlayer.seekTo(currentTrackIndex, playbackPosition)
//        exoPlayer.prepare()
//        exoPlayer.playWhenReady = true
//    }
//}
