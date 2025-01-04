package com.example.mustag.ui.albums

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
        Text(
            text = album.artist,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Light
        )
    }
}


@Composable
fun AlbumsScreen(viewModel: AlbumsViewModel = hiltViewModel()) {
    val albumList by viewModel.albumList.collectAsState()

    if (albumList.isEmpty()) {
        Text(text = "No albums available", modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center)
    } else {
        LazyVerticalGrid(
            columns =  GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier.padding(start = 5.dp, end = 5.dp)
        ) {
            items(albumList) { album ->
                AlbumItem(album = album)
            }
        }
    }
}