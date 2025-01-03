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
        // Добавляем альбом
        val albumId = albumDao.insert(
            Album(name = audio.displayName, year = 0)
        )

        Log.e("song -- ", "${audio.title}, ${audio.duration}, ${audio.artistNames}")
        // Добавляем песню
        val existingSong = songDao.getSongById(audio.id)
        if (existingSong == null){
            val songId = songDao.insert(
                Song(uri = audio.uri, id_song = audio.id, title = audio.title, album_id = albumId, duration = audio.duration, displayName = audio.displayName)
            )

            // Добавляем исполнителей
            audio.artistNames.forEach { artistName ->
                // Проверяем, существует ли исполнитель
                val existingArtist = artistDao.getArtistByName(artistName)
                val artistId = existingArtist?.id_artist
                    ?: artistDao.insert(Artist(name = artistName))

                // Добавляем связь песня-исполнитель
                songDao.insertSongArtist(
                    SongArtist(id_song = audio.id, id_artist = artistId)
                )
            }
        }
    }
}