package com.example.mustag.ui.album

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.mustag.Navigation
import com.example.mustag.ui.albums.songFormat
import com.example.mustag.ui.audio.AudioItem
import com.example.mustag.ui.audio.AudioUIEvents
import com.example.mustag.ui.audio.AudioUIState
import com.example.mustag.ui.audio.AudioViewModel
import com.example.mustag.ui.audio.BottomBarPlayer
import com.example.mustag.ui.audio.PlayerIconItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Album(
    navController: NavController,
    viewModel: AudioViewModel,
    albumViewModel: AlbumViewModel = hiltViewModel(),
    albumId: Long
){
    val progress by viewModel.progress.collectAsState()
    val isAudioPlaying by viewModel.isPlaying.collectAsState()
    val audioList by viewModel.audioList.collectAsState()
    val currentPlayingAudio by viewModel.currentSelectedAudio.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val albumAudioList by albumViewModel.albumAudioList.collectAsState()

    LaunchedEffect(key1 = albumId) {
        albumViewModel.albumId = albumId
        albumViewModel.loadSongs()
    }

    Log.e("SYNC", "ALBUUM_ID is ${albumViewModel.albumId}")
    Log.e("SYNC", "ALBUUM_ID is ${albumViewModel.albumId}")

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
            item {
                if (albumAudioList.size != 0){
                    Row(modifier = Modifier.padding(10.dp)){
                        albumAudioList[0].artwork?.let { artworkBytes ->
                            val bitmap = BitmapFactory.decodeByteArray(artworkBytes, 0, artworkBytes.size)
                            val imageBitmap = bitmap.asImageBitmap()

                            Image(
                                bitmap = imageBitmap,
                                contentDescription = "cover",
                                modifier = Modifier
                                    .width(200.dp)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                ,
                                contentScale = ContentScale.FillBounds
                            )
                        } ?: PlayerIconItem(
                            modifier = Modifier.width(200.dp),
                            icon = Icons.Default.MusicNote,
                            borderStroke = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        ) {}

                        Spacer(modifier = Modifier.size(20.dp))

                        Column (){
                            Text(
                                text = albumAudioList[0].album,
                                style = MaterialTheme.typography.titleMedium,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 3
                            )
                            Spacer(modifier = Modifier.size(10.dp))

                            Text(
                                text = albumAudioList[0].artistNames[0],
                                style = MaterialTheme.typography.titleSmall,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 2
                            )

                            Spacer(modifier = Modifier.size(10.dp))

                            Text(
                                text = albumAudioList.size.toString() + " " + songFormat(albumAudioList.size),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Light,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 2
                            )
                        }
                    }
                }

            }
            itemsIndexed(albumAudioList) { index, audio ->
                AudioItem(
                    audio = audio,
                    number = index + 1,
                    onItemClick = {
                        viewModel.setAudioList(albumAudioList)
                        viewModel.updateMediaItems()
                        viewModel.onUiEvents(AudioUIEvents.SelectedAudioChange(index))
                    }
                )
            }
        }
    }
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
                    .background(color = Color.DarkGray, shape = RoundedCornerShape(10.dp))
                    .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
            ) {
                Text(
                    text = "Песни",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
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

        item {
            Box(
                modifier = Modifier
                    .background(color = Color.Gray, shape = RoundedCornerShape(10.dp))
                    .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                    .clickable { navController.navigate(Navigation.ARTISTS.toString()) }
            ) {
                Text(
                    text = "Исполнители",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        }
    }
}