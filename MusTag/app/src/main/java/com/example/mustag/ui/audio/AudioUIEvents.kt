package com.example.mustag.ui.audio

sealed class AudioUIEvents {
    object PlayPause : AudioUIEvents()
    data class SelectedAudioChange(val index: Int) : AudioUIEvents()
    data class SeekTo(val position: Float) : AudioUIEvents()
    object SeekToNext : AudioUIEvents()
    object Backward : AudioUIEvents()
    object Forward : AudioUIEvents()
    data class UpdateProgress(val newProgress: Float) : AudioUIEvents()
}