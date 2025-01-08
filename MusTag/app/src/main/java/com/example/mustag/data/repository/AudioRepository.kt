package com.example.mustag.data.repository

import com.example.mustag.data.db.Album
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
    suspend fun getAllSongs(): List<Song> = songDao.getAllSongs().reversed()

    suspend fun getAlbum(name: String): Long {
        return albumDao.getAlbumByName(name)!!.id_album
    }

    suspend fun getSongsOnAlbum(albumId: Long): List<Song>{
        return songDao.getSongsByAlbum(albumId).reversed()
    }

    suspend fun getArtistsNamesBySong(songId: Long): List<String>{
        return artistDao.getArtistsBySong(songId).map { it.name }
    }

    suspend fun getArtistName(artistId: Long): String{
        val artist = artistDao.getArtist(artistId) ?: return ""
        return artist.name
    }

    suspend fun getAllAlbums(): List<Album> = albumDao.getAlbums()

    suspend fun getAllArtists(): List<Artist> = artistDao.getArtists()

    suspend fun getAlbumName(albumId: Long): String{
        val album = albumDao.getAlbumById(albumId) ?: return ""
        return album.name
    }

    suspend fun getAlbumSongsCnt(albumId: Long): Int{
        return songDao.getSongsByAlbum(albumId).size
    }

    suspend fun getAlbumsArtistCnt(artistId: Long) : Int {
        return albumDao.getAlbumsByArtist(artistId).size
    }
}