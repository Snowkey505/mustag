package com.example.mustag.ui.artists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mustag.data.local.model.AlbumInfo
import com.example.mustag.data.local.model.ArtistInfo
import com.example.mustag.data.repository.AudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistsViewModel @Inject constructor(
    private val repository: AudioRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _artistsList = MutableStateFlow<List<ArtistInfo>>(emptyList())
    val artistsList: StateFlow<List<ArtistInfo>> = _artistsList

    init {
        loadArtistsData()
    }

    private fun loadArtistsData() {
        viewModelScope.launch {
            val artistsFromDb = repository.getAllArtists()
            _artistsList.value = artistsFromDb.map { artist ->
                ArtistInfo(
                    id = artist.id_artist,
                    name = artist.name,
                    albumsCnt = repository.getAlbumsArtistCnt(artist.id_artist),
                    artwork = artist.artwork
                )
            }
        }
    }
}