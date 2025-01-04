package com.example.mustag.ui.audio

sealed class AudioUIState {
    object Initial : AudioUIState()
    object Ready : AudioUIState()
}