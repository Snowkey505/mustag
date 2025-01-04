package com.example.mustag.data

import android.util.Log
import com.example.mustag.data.db.Album
import com.example.mustag.data.db.AlbumDao
import com.example.mustag.data.db.Artist
import com.example.mustag.data.db.ArtistDao
import com.example.mustag.data.db.Song
import com.example.mustag.data.db.SongArtist
import com.example.mustag.data.db.SongDao
import com.example.mustag.data.local.ContentResolverHelper

suspend fun syncAudioData(
    helper: ContentResolverHelper,
    songDao: SongDao,
    albumDao: AlbumDao,
    artistDao: ArtistDao
) {
    val audioList = helper.getAudioData()

    audioList.forEach { audio ->
        Log.e("SYNC", "Processing audio: ${audio.title}, ${audio.duration}, ${audio.artistNames}, ${audio.album}")

        try {
            val existingSong = songDao.getSongById(audio.id)
            if (existingSong == null) {
                Log.e("SYNC", "Song not found in DB. Proceeding to insert.")

                var artistId: Long = 0L
                audio.artistNames.forEach { artistName ->
                    if (artistName.isNotEmpty()) {
                        val existingArtist = artistDao.getArtistByName(artistName)
                        artistId = existingArtist?.id_artist
                            ?: artistDao.insert(Artist(name = artistName)).also {
                                Log.e("SYNC", "Artist inserted with ID: $it")
                            }
                    }
                }

                val albumName = audio.album.ifEmpty { "Unknown Album" }
                val existingAlbum = albumDao.getAlbumByName(albumName)
                val albumId = existingAlbum?.id_album
                    ?: albumDao.insert(
                        Album(name = albumName, year = 0, artist_id = artistId, artwork = audio.artwork)
                    ).also {
                        Log.e("SYNC", "Album inserted with ID: $it")
                    }

                songDao.insert(
                    Song(
                        uri = audio.uri,
                        id_song = audio.id,
                        title = audio.title.ifEmpty { "Unknown Title" },
                        album_id = albumId,
                        duration = audio.duration,
                        displayName = audio.displayName.ifEmpty { "Unknown Display Name" },
                        artwork = audio.artwork
                    )
                ).also {
                    Log.e("SYNC", "Song inserted with ID: $it")
                }

                songDao.insertSongArtist(
                    SongArtist(id_song = audio.id, id_artist = artistId)
                )
            }
        } catch (e: Exception) {
            Log.e("SYNC", "Error processing audio: ${e.message}", e)
        }
    }
}