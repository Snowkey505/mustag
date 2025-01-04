package com.example.mustag.ui.albums

import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.mustag.data.local.model.AlbumInfo
import com.example.mustag.data.local.model.Audio
import com.example.mustag.data.repository.AudioRepository
import com.example.mustag.player.service.JetAudioServiceHandler
import com.example.mustag.player.service.JetAudioState
import com.example.mustag.player.service.PlayerEvent
import com.example.mustag.ui.audio.AudioUIEvents
import com.example.mustag.ui.audio.AudioUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val repository: AudioRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // Используем StateFlow для списка альбомов
    private val _albumList = MutableStateFlow<List<AlbumInfo>>(emptyList())
    val albumList: StateFlow<List<AlbumInfo>> = _albumList

    init {
        loadAlbumsData()
    }

    private fun loadAlbumsData() {
        viewModelScope.launch {
            val albumsFromDb = repository.getAllAlbums()
            _albumList.value = albumsFromDb.map { album ->
                AlbumInfo(
                    id = album.id_album,
                    title = album.name,
                    artist = repository.getArtistName(album.artist_id),
                    year = album.year,
                    artwork = album.artwork
                )
            }
        }
    }
}
