package com.example.mustag.ui.audio

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    // Состояния для длительности, прогресса и состояния воспроизведения
    private val _duration = MutableStateFlow(0L)
    var duration: StateFlow<Long> = _duration

    private val _progress = MutableStateFlow(0f)
    var progress: StateFlow<Float> = _progress

    private val _progressString = MutableStateFlow("00:00")
    var progressString: StateFlow<String> = _progressString

    private val _isPlaying = MutableStateFlow(false)
    var isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentSelectedAudio = MutableStateFlow(audioDummy)
    var currentSelectedAudio: StateFlow<Audio> = _currentSelectedAudio

    private val _audioList = MutableStateFlow(emptyList<Audio>())
    var audioList: StateFlow<List<Audio>> = _audioList

    private val _uiState: MutableStateFlow<AudioUIState> = MutableStateFlow(AudioUIState.Initial)
    val uiState: StateFlow<AudioUIState> = _uiState.asStateFlow()

    private var isDataLoaded = false

    // Новый список для медиа-элементов
    private val _mediaItemsList = MutableStateFlow<List<MediaItem>>(emptyList())
    val mediaItemsList: StateFlow<List<MediaItem>> = _mediaItemsList

    init {
        Log.e("a", "INIT_VIEW_MODEL, data_is_loaded is $isDataLoaded")
        // Подключаемся к AudioService
        observeAudioService()
        if (!isDataLoaded)
        {
            loadAudioData()
            isDataLoaded = true
        }
    }

    private fun loadAudioData() {
        viewModelScope.launch {
            // Загружаем все песни из базы данных
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
            isDataLoaded = true // Устанавливаем флаг, что данные загружены

            // Обновляем список медиа-элементов
            updateMediaItems()
        }
    }

    // Функция для обновления списка медиа-элементов
    private fun updateMediaItems() {
        val mediaItems = _audioList.value.map { audio ->
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
        }
        _mediaItemsList.value = mediaItems
    }

    fun setMediaItems() {
        Log.e("SYNC", "MEDIA_BUILDING")
        audioServiceHandler.setMediaItemList(_mediaItemsList.value)
    }

    private fun observeAudioService() {
        viewModelScope.launch {
            audioServiceHandler.audioState.collectLatest { mediaState ->
                when (mediaState) {
                    JetAudioState.Initial -> {
                        _uiState.value = AudioUIState.Initial
                        Log.e("SYNC", "PLAYER_INITIAL")
                    }
                    is JetAudioState.Buffering -> {
                        Log.e("SYNC", "PLAYER_BUFFERING")
                        calculateProgressValue(mediaState.progress)
                    }
                    is JetAudioState.Playing -> {
                        Log.e("SYNC", "PLAYER_IS_PLAYING")
                        _isPlaying.value = mediaState.isPlaying
                        // Обновляем текущий выбранный аудио-файл, если индекс изменился
                        _currentSelectedAudio.value = _audioList.value.getOrNull(mediaState.currentMediaItemIndex)!!
                    }
                    is JetAudioState.Progress -> {
                        Log.e("SYNC", "PLAYER_PROGRESS")
                        calculateProgressValue(mediaState.progress)
                    }
                    is JetAudioState.Ready -> {
                        Log.e("SYNC", "PLAYER_READY")
                        _duration.value = mediaState.duration
                        _uiState.value = AudioUIState.Ready
                    }
                    is JetAudioState.CurrentPlaying -> {
                        Log.e("SYNC", "PLAYER_CURRENT")
                        // Устанавливаем текущий элемент на основе индекса
                        _currentSelectedAudio.value = _audioList.value.getOrNull(mediaState.mediaItemIndex)!!
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
            AudioUIEvents.SeekToPrevious -> audioServiceHandler.onPlayerEvents(PlayerEvent.SeekToPrevious)
            is AudioUIEvents.PlayPause -> {
                Log.e("SYNC", "UI_PLAY_PAUSE")
                audioServiceHandler.onPlayerEvents(PlayerEvent.PlayPause)
            }
            is AudioUIEvents.SeekTo -> {
                Log.e("SYNC", "UI_SEEK_TO")
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.SeekTo,
                    seekPosition = ((_duration.value * uiEvents.position) / 100f).toLong()
                )
            }
            is AudioUIEvents.SelectedAudioChange -> {
                setMediaItems()
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.SelectedAudioChange,
                    selectedAudioIndex = uiEvents.index
                )
            }
            is AudioUIEvents.UpdateProgress -> {
                Log.e("SYNC", "UPDATE_PROGRESS")
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.UpdateProgress(uiEvents.newProgress)
                )
                _progress.value = uiEvents.newProgress
            }
        }
    }

    fun formatDuration(duration: Long): String {
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
