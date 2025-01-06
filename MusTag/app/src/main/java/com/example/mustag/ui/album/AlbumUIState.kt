package com.example.mustag.ui.album

sealed class AlbumUIState {
    object Initial : AlbumUIState()
    object Ready : AlbumUIState()
    object Playing : AlbumUIState()
}