package com.example.mustag.data.repository

import com.example.mustag.data.db.AlbumDao
import com.example.mustag.data.db.Artist
import com.example.mustag.data.db.ArtistDao
import com.example.mustag.data.db.Song
import com.example.mustag.data.db.SongDao
import com.example.mustag.data.local.ContentResolverHelper
import com.example.mustag.data.local.model.Audio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AudioRepository @Inject constructor(
    private val songDao: SongDao,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
) {
    suspend fun getAllSongs(): List<Song> = songDao.getAllSongs()

    suspend fun getAlbumNameById(albumId: Long): String {
        return albumDao.getAlbumById(albumId)?.name ?: "Unknown"
    }

    suspend fun getArtistsNamesBySong(songId: Long): List<String>{
        return artistDao.getArtistsBySong(songId).map { it.name }
    }
}