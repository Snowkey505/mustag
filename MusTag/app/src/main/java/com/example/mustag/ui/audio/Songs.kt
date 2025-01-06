package com.example.mustag.ui.audio

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsEndWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.mustag.Navigation
import com.example.mustag.data.local.model.Audio
import kotlin.math.floor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsScreen(navController: NavController, viewModel: AudioViewModel, startService: () -> Unit) {
    val progress by viewModel.progress.collectAsState()
    val isAudioPlaying by viewModel.isPlaying.collectAsState()
    val audioList by viewModel.audioList.collectAsState()
    val currentPlayingAudio by viewModel.currentSelectedAudio.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = audioList) {
        if (audioList.isNotEmpty()) {
            startService()
        }
    }

    Log.e("SYNC", "$audioList")

    Scaffold(
        topBar = { TopPanel(navController) },
        bottomBar = {
            if (uiState == AudioUIState.Playing) {
                Log.e("SYNC", "$isAudioPlaying, AUDIO = ${currentPlayingAudio.displayName}, $progress")
                BottomBarPlayer(
                    progress = progress,
                    audio = currentPlayingAudio,
                    isAudioPlaying = isAudioPlaying,
                    viewModel = viewModel,
                    navController = navController,
                    onProgress = { newProgress ->
                        viewModel.onUiEvents(AudioUIEvents.SeekTo(newProgress))
                    },
                    onStart = {
                        viewModel.onUiEvents(AudioUIEvents.PlayPause)
                    },
                    onNext = {
                        viewModel.onUiEvents(AudioUIEvents.SeekToNext)
                    },
                    onPrevious = {
                        viewModel.onUiEvents(AudioUIEvents.SeekToPrevious)
                    }
                )
            } else {
                null
            }
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues
        ) {
            itemsIndexed(audioList) { index, audio ->
                AudioItem(
                    audio = audio,
                    number = index + 1,
                    onItemClick = {
                        viewModel.onUiEvents(AudioUIEvents.SelectedAudioChange(index))
                    }
                )
            }
        }
    }
}

@Composable
fun AudioItem(
    audio: Audio,
    number: Int,
    onItemClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp, top = 5.dp)
            .clickable {
                onItemClick()
            },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Box (modifier = Modifier.width(40.dp)){
                Column (horizontalAlignment = Alignment.CenterHorizontally) {
                    audio.artwork?.let { artworkBytes ->
                        val bitmap = BitmapFactory.decodeByteArray(artworkBytes, 0, artworkBytes.size)
                        val imageBitmap = bitmap.asImageBitmap()

                        Image(
                            bitmap = imageBitmap,
                            contentDescription = "cover",
                            modifier = Modifier
                                .fillMaxWidth(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(1.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentScale = ContentScale.FillBounds
                        )
                    } ?: PlayerIconItem(
                        icon = Icons.Default.MusicNote,
                        borderStroke = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    ) {}
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = audio.title,
                    style = MaterialTheme.typography.titleMedium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Text(
                    text = audio.artistNames.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

            }
            Text(
                modifier = Modifier.padding(end = 10.dp),
                text = timeStampToDuration(audio.duration.toLong()),
                style = MaterialTheme.typography.bodyMedium
            )
        }

    }
}

fun timeStampToDuration(position: Long): String {
    val totalSecond = floor(position / 1E3).toInt()
    val minutes = totalSecond / 60
    val remainingSeconds = totalSecond - (minutes * 60)
    return if (position < 0) "--:--"
    else "%d:%02d".format(minutes, remainingSeconds)
}


@Composable
private fun TopPanel(navController: NavController) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .background(color = Color.White, shape = RoundedCornerShape(10.dp))
                    .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
            ) {
                Text(
                    text = "Песни",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
            }
        }
        item {
            Box(
                modifier = Modifier
                    .background(color = Color.DarkGray, shape = RoundedCornerShape(10.dp))
                    .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                    .clickable { navController.navigate(Navigation.ALBUMS.toString()) }
            ) {
                Text(
                    text = "Альбомы",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomBarPlayer(
    progress: Float,
    onProgress: (Float) -> Unit,
    audio: Audio,
    viewModel: AudioViewModel,
    navController: NavController,
    isAudioPlaying: Boolean,
    onStart: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () ->Unit
) {

    BottomAppBar(
        modifier = Modifier.height(80.dp),
        content = {
            Column(
                modifier = Modifier.padding(bottom = 5.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth().padding(start=4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SongInfo(
                        audio = audio,
                        navController = navController,
                        viewModel = viewModel,
                        modifier = Modifier.weight(1f)
                    )

                    MediaPlayerController(
                        isAudioPlaying = isAudioPlaying,
                        onStart = onStart,
                        onNext = onNext,
                        onPrevious = onPrevious
                    )

                }
                Slider(
                    modifier = Modifier.fillMaxWidth(),
                    value = progress,
                    onValueChange = { onProgress(it) },
                    valueRange = 0f..100f
                )
            }
        }
    )
}

@Composable
fun MediaPlayerController(
    isAudioPlaying: Boolean,
    onStart: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .width(100.dp)
            .padding(start = 4.dp, end = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.SkipPrevious,
            modifier = Modifier.clickable {
                onPrevious()
            },
            contentDescription = null
        )
        Spacer(modifier = Modifier.size(5.dp))
        PlayerIconItem(
            icon = if (isAudioPlaying) Icons.Default.Pause
            else Icons.Default.PlayArrow
        ) {
            onStart()
        }
        Spacer(modifier = Modifier.size(5.dp))
        Icon(
            imageVector = Icons.Default.SkipNext,
            modifier = Modifier.clickable {
                onNext()
            },
            contentDescription = null
        )
    }
}

@Composable
fun SongInfo(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: AudioViewModel,
    audio: Audio?,
) {
    val albumId by viewModel.currentAlbum.collectAsState()

    Row(
        modifier = modifier.padding(start = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        audio!!.artwork?.let { artworkBytes ->
            val bitmap = BitmapFactory.decodeByteArray(artworkBytes, 0, artworkBytes.size)
            val imageBitmap = bitmap.asImageBitmap()

            Image(
                bitmap = imageBitmap,
                contentDescription = "cover",
                modifier = Modifier
                    .width(30.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { navController.navigate("${Navigation.ALBUM}/${albumId}") },
                contentScale = ContentScale.FillBounds
            )
        } ?: PlayerIconItem(
            icon = Icons.Default.MusicNote,
            borderStroke = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface
            )
        ) {}
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = audio!!.title,
            modifier = Modifier.clickable { navController.navigate(Navigation.PLAYER.toString()) },
            fontWeight = FontWeight.Normal,
            style = MaterialTheme.typography.bodyMedium,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = audio!!.artistNames.joinToString(", "),
            fontWeight = FontWeight.Normal,
            style = MaterialTheme.typography.bodySmall,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

@Composable
fun PlayerIconItem(
    modifier: Modifier = Modifier.padding(start=5.dp),
    icon: ImageVector,
    borderStroke: BorderStroke? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    Surface(
        shape = CircleShape,
        border = borderStroke,
        modifier = Modifier
            .clip(CircleShape)
            .clickable {
                onClick()
            },
        contentColor = color,
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier.padding(4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        }
    }
}

//@Preview(showSystemUi = true)
//@Composable
//fun HomeScreenPrev() {
//    MusTagTheme(darkTheme = true) {
//        HomeScreen()
//    }
//}