package com.example.mustag

//import androidx.lifecycle.lifecycleScope
//import com.example.mustag.data.db.AlbumDao
//import com.example.mustag.data.db.SongDao
//import com.example.mustag.data.local.ContentResolverHelper
//import com.example.mustag.data.syncAudioData
import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mustag.data.db.AlbumDao
import com.example.mustag.data.db.ArtistDao
import com.example.mustag.data.db.SongDao
import com.example.mustag.data.local.ContentResolverHelper
import com.example.mustag.data.syncAudioData
import com.example.mustag.player.service.JetAudioService
import com.example.mustag.ui.album.Album
import com.example.mustag.ui.albums.AlbumsScreen
import com.example.mustag.ui.artists.ArtistsScreen
import com.example.mustag.ui.audio.AudioViewModel
import com.example.mustag.ui.audio.SongsScreen
import com.example.mustag.ui.player.Player
import com.example.mustag.ui.theme.MusTagTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


enum class Navigation(val route: String) {
    SONGS("songs"),
    ALBUMS("albums"),
    PLAYER("player"),
    ALBUM("album"),
    ARTISTS("artists")
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var contentResolverHelper: ContentResolverHelper
    @Inject lateinit var songDao: SongDao
    @Inject lateinit var albumDao: AlbumDao
    @Inject lateinit var artistDao: ArtistDao

    private var isServiceRunning = false

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            syncAudioData(contentResolverHelper, songDao, albumDao, artistDao, context = this@MainActivity)
        }

        setStatusBarColor(
            color = Color(0xFF0D0D0D),
            darkIcons = false
        )

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

                // Навигация и навигационный граф
                val navController = rememberNavController()
                val audioViewModel: AudioViewModel = hiltViewModel()

                NavHost(
                    navController = navController,
                    startDestination = Navigation.ALBUMS.toString()
                ) {
                    composable(Navigation.SONGS.toString()) {
                        SongsScreen(navController = navController, viewModel = audioViewModel,
                            startService = ::startService)
                    }
                    composable(Navigation.ALBUMS.toString()) {
                        AlbumsScreen(navController = navController, viewModel = audioViewModel)
                    }
                    composable(Navigation.ARTISTS.toString()) {
                        ArtistsScreen(navController = navController, viewModel = audioViewModel)
                    }
                    composable(Navigation.PLAYER.toString()) {
                        Player(navController = navController, viewModel = audioViewModel)
                    }

                    composable(
                        route = "${Navigation.ALBUM}/{albumId}",
                        arguments = listOf(navArgument("albumId") { type = NavType.LongType }
                        )
                    ) { backStackEntry ->
                        val albumId = backStackEntry.arguments?.getLong("albumId")
                        Album(navController = navController, viewModel = audioViewModel, albumId = albumId!!,)
                    }
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

    private fun stopService() {
        if (isServiceRunning) {
            val intent = Intent(application, JetAudioService::class.java)
            application.stopService(intent)
            isServiceRunning = false
        }
    }

    override fun onDestroy() {
        stopService()
        super.onDestroy()
    }
}


fun ComponentActivity.setStatusBarColor(color: Color, darkIcons: Boolean) {
    window.statusBarColor = color.toArgb()
    WindowCompat.getInsetsController(window, window.decorView)?.isAppearanceLightStatusBars = darkIcons
}