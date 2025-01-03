package com.example.mustag.ui.audio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.mustag.ui.theme.MusTagTheme
import com.example.mustag.data.local.model.Audio
import kotlin.math.floor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    progress: Float,
    onProgress: (Float) -> Unit,
    isAudioPlaying: Boolean,
    currentPlayingAudio: Audio,
    audiList: List<Audio>,
    onStart: () -> Unit,
    onItemClick: (Int) -> Unit,
    onNext: () -> Unit,
) {
    Scaffold(
        bottomBar = {
            BottomBarPlayer(
                progress = progress,
                onProgress = onProgress,
                audio = currentPlayingAudio,
                onStart = onStart,
                onNext = onNext,
                isAudioPlaying = isAudioPlaying
            )
        }
    ) {
        LazyColumn(
            contentPadding = it
        ) {
            itemsIndexed(audiList) { index, audio ->
                AudioItem(
                    audio = audio,
                    number = index + 1,
                    onItemClick = { onItemClick(index) }
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
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = number.toString(),
                        textAlign = TextAlign.Center
                    )
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
                    overflow = TextOverflow.Clip,
                    maxLines = 1
                )
                Text(
                    text = audio.artist,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
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

private fun timeStampToDuration(position: Long): String {
    val totalSecond = floor(position / 1E3).toInt()
    val minutes = totalSecond / 60
    val remainingSeconds = totalSecond - (minutes * 60)
    return if (position < 0) "--:--"
    else "%d:%02d".format(minutes, remainingSeconds)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomBarPlayer(
    progress: Float,
    onProgress: (Float) -> Unit,
    audio: Audio,
    isAudioPlaying: Boolean,
    onStart: () -> Unit,
    onNext: () -> Unit,
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
                        modifier = Modifier
                    )

                    MediaPlayerController(
                        isAudioPlaying = isAudioPlaying,
                        onStart = onStart,
                        onNext = onNext
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
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(start = 4.dp, end = 4.dp)
    ) {
        PlayerIconItem(
            icon = if (isAudioPlaying) Icons.Default.Pause
            else Icons.Default.PlayArrow
        ) {
            onStart()
        }
        Spacer(modifier = Modifier.size(8.dp))
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
    audio: Audio,
) {
    Row(
        modifier = modifier.padding(start = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayerIconItem(
            icon = Icons.Default.MusicNote,
            borderStroke = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface
            )
        ) {}
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = audio.title,
            fontWeight = FontWeight.Normal,
            style = MaterialTheme.typography.bodyMedium,
            overflow = TextOverflow.Clip,
            maxLines = 1
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = audio.artist,
            fontWeight = FontWeight.ExtraLight,
            style = MaterialTheme.typography.bodyMedium,
            overflow = TextOverflow.Clip,
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

@Preview(showSystemUi = true)
@Composable
fun HomeScreenPrev() {
    MusTagTheme(darkTheme = true) {
        HomeScreen(
            progress = 50f,
            onProgress = {},
            isAudioPlaying = true,
            audiList = listOf(
                Audio("".toUri(), "Nightcall", 0L, "Kavinsky", "", 0, "Nightcall"),
                Audio("".toUri(), "Title Two", 0L, "Unknown", "", 0, "Title two"),
            ),
            currentPlayingAudio = Audio("".toUri(), "Nightcall", 0L, "Kavinsky", "", 0, "Nightcall"),
            onStart = {},
            onItemClick = {},
            onNext = {}
        )
    }
}














