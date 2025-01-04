package com.example.mustag.ui.albums

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.mustag.Navigation
import com.example.mustag.data.local.model.AlbumInfo

@Composable
fun AlbumItem(album: AlbumInfo) {
    Column(
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth()
    ) {
        album.artwork?.let { artworkBytes ->
            val bitmap = BitmapFactory.decodeByteArray(artworkBytes, 0, artworkBytes.size)
            val imageBitmap = bitmap.asImageBitmap()

            Image(
                bitmap = imageBitmap,
                contentDescription = "Album Artwork",
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentScale = ContentScale.FillBounds
            )
        } ?: Text(
            text = "No Artwork",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(16.dp)
        )

        Text(
            text = album.title,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
        )
        Row{
            Text(
                text = album.artist,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Light,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = album.songs_cnt.toString() + " " + songFormat(album.songs_cnt),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Light
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(navController: NavController, viewModel: AlbumsViewModel = hiltViewModel()) {
    val albumList by viewModel.albumList.collectAsState()
    Scaffold(
        topBar = { TopPanel(navController) })
    { paddingValues ->
        if (albumList.isEmpty()) {
            Text(text = "", modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier
                    .padding(paddingValues).padding(start = 5.dp, end = 5.dp)
            ) {
                items(albumList) { album ->
                    AlbumItem(album = album)
                }
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
                    .clickable { navController.navigate(Navigation.SONGS.toString()) }
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
                    .background(color = Color.White, shape = RoundedCornerShape(10.dp))
                    .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
            ) {
                Text(
                    text = "Альбомы",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
            }
        }
    }
}

fun songFormat(songsCnt: Int): String{
    if ((songsCnt >= 5 && songsCnt <= 20) || (songsCnt % 10) >= 5 || (songsCnt % 10) == 0)
        return "песен"
    else if (songsCnt % 10 == 1)
        return "песня"
    return "песни"
}