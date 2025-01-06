package com.example.mustag.ui.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mustag.data.local.model.Audio
import com.example.mustag.data.repository.AudioRepository
import com.example.mustag.ui.audio.AudioUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor (
    private val repository: AudioRepository
): ViewModel(){

    private val _albumAudioList = MutableStateFlow<List<Audio>>(emptyList())
    val albumAudioList: StateFlow<List<Audio>> = _albumAudioList

    private val _uiState: MutableStateFlow<AlbumUIState> = MutableStateFlow(AlbumUIState.Initial)
    val uiState: StateFlow<AlbumUIState> = _uiState.asStateFlow()

    var albumId = -1L
    var isLoaded = false


    fun loadSongs(){
        viewModelScope.launch {
            val songsFromDb = repository.getSongsOnAlbum(albumId = albumId)
            _albumAudioList.value = songsFromDb.map { song ->
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
        }
    }
}