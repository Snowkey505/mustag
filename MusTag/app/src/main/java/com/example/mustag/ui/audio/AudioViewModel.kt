package com.example.mustag.ui.audio

import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.mustag.data.local.model.Audio
import com.example.mustag.data.repository.AudioRepository
import com.example.mustag.player.service.JetAudioServiceHandler
import com.example.mustag.player.service.JetAudioState
import com.example.mustag.player.service.PlayerEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private val audioDummy = Audio(
    "".toUri(), "", 0L, listOf(""), "", 0, "", "", null,
)

@HiltViewModel
class AudioViewModel @Inject constructor(
    private val audioServiceHandler: JetAudioServiceHandler,
    private val repository: AudioRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress

    private val _progressString = MutableStateFlow("00:00")
    val progressString: StateFlow<String> = _progressString

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentSelectedAudio = MutableStateFlow(audioDummy)
    val currentSelectedAudio: StateFlow<Audio> = _currentSelectedAudio

    private val _audioList = MutableStateFlow(emptyList<Audio>())
    val audioList: StateFlow<List<Audio>> = _audioList

    private val _uiState: MutableStateFlow<AudioUIState> = MutableStateFlow(AudioUIState.Initial)
    val uiState: StateFlow<AudioUIState> = _uiState.asStateFlow()

    init {
        loadAudioData()
        observeAudioService()
    }

    private fun loadAudioData() {
        viewModelScope.launch {
            val songsFromDb = repository.getAllSongs()
            _audioList.value = songsFromDb.map { song ->
                Audio(
                    uri = song.uri,
                    id = song.id_song,
                    title = song.title,
                    displayName = song.displayName,
                    artistNames = repository.getArtistsNamesBySong(song.id_song),
                    duration = song.duration,
                    album = repository.getAlbumName(song.album_id),
                    artwork = song.artwork,
                    data = ""
                )
            }
            setMediaItems()
        }
    }

    private fun setMediaItems() {
        _audioList.value.map { audio ->
            MediaItem.Builder()
                .setUri(audio.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setAlbumArtist(audio.artistNames.joinToString(";"))
                        .setDisplayTitle(audio.title)
                        .setSubtitle(audio.displayName)
                        .build()
                )
                .build()
        }.also {
            audioServiceHandler.setMediaItemList(it)
        }
    }

    private fun observeAudioService() {
        viewModelScope.launch {
            audioServiceHandler.audioState.collectLatest { mediaState ->
                when (mediaState) {
                    JetAudioState.Initial -> _uiState.value = AudioUIState.Initial
                    is JetAudioState.Buffering -> calculateProgressValue(mediaState.progress)
                    is JetAudioState.Playing -> _isPlaying.value = mediaState.isPlaying
                    is JetAudioState.Progress -> calculateProgressValue(mediaState.progress)
                    is JetAudioState.CurrentPlaying -> {
                        _currentSelectedAudio.value = _audioList.value[mediaState.mediaItemIndex]
                    }
                    is JetAudioState.Ready -> {
                        _duration.value = mediaState.duration
                        _uiState.value = AudioUIState.Ready
                    }
                }
            }
        }
    }

    private fun calculateProgressValue(currentProgress: Long) {
        _progress.value =
            if (currentProgress > 0) ((currentProgress.toFloat() / _duration.value.toFloat()) * 100f)
            else 0f
        _progressString.value = formatDuration(currentProgress)
    }

    fun onUiEvents(uiEvents: AudioUIEvents) = viewModelScope.launch {
        when (uiEvents) {
            AudioUIEvents.Backward -> audioServiceHandler.onPlayerEvents(PlayerEvent.Backward)
            AudioUIEvents.Forward -> audioServiceHandler.onPlayerEvents(PlayerEvent.Forward)
            AudioUIEvents.SeekToNext -> audioServiceHandler.onPlayerEvents(PlayerEvent.SeekToNext)
            is AudioUIEvents.PlayPause -> {
                audioServiceHandler.onPlayerEvents(PlayerEvent.PlayPause)
            }
            is AudioUIEvents.SeekTo -> {
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.SeekTo,
                    seekPosition = ((_duration.value * uiEvents.position) / 100f).toLong()
                )
            }
            is AudioUIEvents.SelectedAudioChange -> {
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.SelectedAudioChange,
                    selectedAudioIndex = uiEvents.index
                )
            }
            is AudioUIEvents.UpdateProgress -> {
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.UpdateProgress(uiEvents.newProgress)
                )
                _progress.value = uiEvents.newProgress
            }
        }
    }

    private fun formatDuration(duration: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onCleared() {
        viewModelScope.launch {
            audioServiceHandler.onPlayerEvents(PlayerEvent.Stop)
        }
        super.onCleared()
    }
}