package com.example.mustag

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
//import androidx.lifecycle.lifecycleScope
//import com.example.mustag.data.db.AlbumDao
//import com.example.mustag.data.db.SongDao
//import com.example.mustag.data.local.ContentResolverHelper
//import com.example.mustag.data.syncAudioData
import com.example.mustag.ui.theme.MusTagTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint
import com.example.mustag.player.service.JetAudioService
import com.example.mustag.ui.audio.AudioViewModel
import com.example.mustag.ui.audio.HomeScreen
import com.example.mustag.ui.audio.UIEvents
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: AudioViewModel by viewModels()
    private var isServiceRunning = false

//    @Inject
//    lateinit var contentResolverHelper: ContentResolverHelper
//    @Inject lateinit var songDao: SongDao
//    @Inject lateinit var albumDao: AlbumDao

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        lifecycleScope.launch {
//            syncAudioData(contentResolverHelper, songDao, albumDao)
//        }

        setContent {
            MusTagTheme {
                val permissionState = rememberPermissionState(
                    permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_AUDIO
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }
                )
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(key1 = lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            permissionState.launchPermissionRequest()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(
                        progress = viewModel.progress,
                        onProgress = { viewModel.onUiEvents(UIEvents.SeekTo(it)) },
                        isAudioPlaying = viewModel.isPlaying,
                        audiList = viewModel.audioList,
                        currentPlayingAudio = viewModel.currentSelectedAudio,
                        onStart = {
                            viewModel.onUiEvents(UIEvents.PlayPause)
                        },
                        onItemClick = {
                            viewModel.onUiEvents(UIEvents.SelectedAudioChange(it))
                            startService()
                        },
                        onNext = {
                            viewModel.onUiEvents(UIEvents.SeekToNext)
                        }
                    )
                }
            }
        }
    }

    private fun startService() {
        if (!isServiceRunning) {
            val intent = Intent(this, JetAudioService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            isServiceRunning = true
        }
    }
}