package com.example.mustag.ui.albums

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mustag.data.local.model.AlbumInfo

@Composable
fun AlbumItem(album: AlbumInfo) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = album.title,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Artist: ${album.artist}",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "Year: ${album.year}",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun AlbumsScreen(viewModel: AlbumsViewModel = hiltViewModel()) {
    // Получаем список альбомов из ViewModel
    val albumList by viewModel.albumList.collectAsState()

    // Проверка, что список альбомов не пуст
    if (albumList.isEmpty()) {
        // Можно показать пустой экран или загрузку
        Text(text = "No albums available", modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center)
    } else {
        // Список альбомов
        LazyColumn {
            items(albumList) { album ->
                AlbumItem(album = album)
            }
        }
    }
}

