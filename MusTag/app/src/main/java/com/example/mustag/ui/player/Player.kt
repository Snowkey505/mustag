package com.example.mustag.ui.player

import android.graphics.BitmapFactory
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mustag.R
import com.example.mustag.ui.audio.AudioUIEvents
import com.example.mustag.ui.audio.AudioViewModel
import com.example.mustag.ui.audio.PlayerIconItem
import com.example.mustag.ui.audio.SongInfo
import com.example.mustag.ui.audio.timeStampToDuration
import okhttp3.internal.concurrent.formatDuration

@Composable
fun Player(
    navController: NavController,
    viewModel: AudioViewModel){

    val progress by viewModel.progress.collectAsState()
    val progressString by viewModel.progressString.collectAsState()
    val isAudioPlaying by viewModel.isPlaying.collectAsState()
    val audioList by viewModel.audioList.collectAsState()
    val audio by viewModel.currentSelectedAudio.collectAsState()

    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = MaterialTheme.colorScheme.background)
        ){
        Column {

            Row(modifier = Modifier.padding(10.dp)){
                Image(
                    painter = painterResource(R.drawable.back_v),
                    modifier = Modifier.height(30.dp).clickable { navController.popBackStack() },
                    contentDescription = ""
                )
                Text(
                    text = audio.album,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(20.dp))
                Image(
                    painter = painterResource(R.drawable.dots_menu),
                    modifier = Modifier.height(30.dp),
                    contentDescription = ""
                )
            }

            Spacer(modifier = Modifier.weight(3f))
            audio!!.artwork?.let { artworkBytes ->
                val bitmap = BitmapFactory.decodeByteArray(artworkBytes, 0, artworkBytes.size)
                val imageBitmap = bitmap.asImageBitmap()

                Image(
                    bitmap = imageBitmap,
                    contentDescription = "cover",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(10.dp))
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
            ){
                Image(
                    painter = painterResource(R.drawable.queue_1),
                    modifier = Modifier.height(60.dp),
                    alignment = Alignment.Center,
                    contentDescription = ""
                )
                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(
                        text = audio.title,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
//                    Spacer(modifier = Modifier.padding(4.dp))

                    Text(
                        text = audio.artistNames.joinToString(", "),
                        color = Color.Gray,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Normal,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Image(
                    painter = painterResource(R.drawable.add),
                    modifier = Modifier.height(60.dp),
                    alignment = Alignment.Center,
                    contentDescription = ""
                )
            }

            Slider(
                modifier = Modifier.fillMaxWidth(),
                value = progress,
                onValueChange = { newProgress ->
                    viewModel.onUiEvents(AudioUIEvents.SeekTo(newProgress)) },
                valueRange = 0f..100f
            )

            Row(modifier = Modifier
                .padding(start = 10.dp, end = 10.dp)){
                Text(
                    text = progressString,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Light,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = timeStampToDuration(audio.duration.toLong()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Light,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            Row(modifier = Modifier.padding(start = 10.dp, end = 10.dp)){
                Image(
                    painter = painterResource(R.drawable.shuffle),
                    modifier = Modifier.height(60.dp),
                    alignment = Alignment.Center,
                    contentDescription = ""
                )
                Spacer(modifier = Modifier.weight(3f))

                Image(
                    painter = painterResource(R.drawable.prev),
                    modifier = Modifier.height(60.dp).clickable { viewModel.onUiEvents(AudioUIEvents.SeekToPrevious) },
                    alignment = Alignment.Center,
                    contentDescription = ""
                )
                Spacer(modifier = Modifier.weight(1f))

                Image(
                    painter = if (isAudioPlaying) painterResource(R.drawable.pause) else painterResource(R.drawable.play) ,
                    modifier = Modifier.height(60.dp).clickable { viewModel.onUiEvents(AudioUIEvents.PlayPause) },
                    alignment = Alignment.Center,
                    contentDescription = ""
                )
                Spacer(modifier = Modifier.weight(1f))

                Image(
                    painter = painterResource(R.drawable.next),
                    modifier = Modifier.height(60.dp).clickable { viewModel.onUiEvents(AudioUIEvents.SeekToNext) },
                    alignment = Alignment.Center,
                    contentDescription = ""
                )
                Spacer(modifier = Modifier.weight(3f))

                Image(
                    painter = painterResource(R.drawable.repeat),
                    modifier = Modifier.height(60.dp),
                    alignment = Alignment.Center,
                    contentDescription = ""
                )


            }
            Spacer(modifier = Modifier.weight(2f))
        }
    }

}