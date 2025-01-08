package com.example.mustag.data

import android.content.Context
import android.util.Log
import com.example.mustag.data.db.Album
import com.example.mustag.data.db.AlbumArtist
import com.example.mustag.data.db.AlbumDao
import com.example.mustag.data.db.Artist
import com.example.mustag.data.db.ArtistDao
import com.example.mustag.data.db.Song
import com.example.mustag.data.db.SongArtist
import com.example.mustag.data.db.SongDao
import com.example.mustag.data.local.ContentResolverHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

suspend fun syncAudioData(
    helper: ContentResolverHelper,
    songDao: SongDao,
    albumDao: AlbumDao,
    artistDao: ArtistDao,
    context: Context
) {

    val dbFile = context.getDatabasePath("audio_database")
    Log.e("SYNC", "Database path: ${dbFile.absolutePath}")

    val dbBackupFile = File(context.filesDir, "audio_database.db")
    Log.e("SYNC", "Backup file path not exist: ${dbBackupFile.absolutePath}")
    if (!dbBackupFile.exists()) {
        Log.e("SYNC", "Backup file not exist")
    }

    if (dbBackupFile.exists()) {
        Log.e("SYNC", "FILE DB FOUND")
        try {
            restoreDatabaseFromBackup(context, "audio_database", dbBackupFile)
            Log.e("SYNC", "Database restored from backup.")
            return // Завершаем выполнение функции
        } catch (e: Exception) {
            Log.e("SYNC", "Failed to restore database from backup: ${e.message}", e)
        }
    }

    val audioList = helper.getAudioData()

    CoroutineScope(Dispatchers.IO).launch {
        audioList.forEach { audio ->
            try {
                Log.e(
                    "SYNC",
                    "Processing audio: ${audio.title}, ${audio.duration}, ${audio.artistNames}, ${audio.album}"
                )

                val existingSong = songDao.getSongById(audio.id)
                if (existingSong == null) {
                    Log.e("SYNC", "Song not found in DB. Proceeding to insert.")

                    var artistId: Long = 0L
                    audio.artistNames.forEach { artistName ->
                        if (artistName.isNotEmpty()) {
                            val existingArtist = artistDao.getArtistByName(artistName)
                            artistId = existingArtist?.id_artist
                                ?: artistDao.insert(
                                    Artist(
                                        name = artistName,
                                        artwork = audio.artwork
                                    )
                                ).also {
                                    Log.e("SYNC", "Artist inserted with ID: $it")
                                }
                        }
                    }

                    val albumName = audio.album.ifEmpty { "Unknown Album" }
                    val existingAlbum = albumDao.getAlbumByName(albumName)
                    val albumId = existingAlbum?.id_album
                        ?: albumDao.insert(
                            Album(
                                name = albumName,
                                year = 0,
                                artist_id = artistId,
                                artwork = audio.artwork
                            )
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

                    audio.artistNames.forEach { artistName ->
                        artistId = artistDao.getArtistByName(artistName)!!.id_artist

                        if (songDao.getSongArtistById(audio.id, artistId) == null) {
                            songDao.insertSongArtist(
                                SongArtist(id_song = audio.id, id_artist = artistId)
                            )
                        }

                        if (albumDao.getAlbumArtistById(albumId, artistId) == null) {
                            albumDao.insertAlbumArtist(
                                AlbumArtist(id_artist = artistId, id_album = albumId)
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SYNC", "Error processing audio: ${e.message}", e)
            }
        }

        try {
            withContext(Dispatchers.IO) {
                backupDatabase(context, "audio_database", dbBackupFile)
            }
            Log.e("SYNC", "Database backed up successfully.")
        } catch (e: Exception) {
            Log.e("SYNC", "Failed to back up database: ${e.message}", e)
        }
    }
}


fun backupDatabase(context: Context, dbName: String, backupFile: File) {
    val dbFile = context.getDatabasePath(dbName)

    if (dbFile.exists()) {
        dbFile.inputStream().use { input ->
            backupFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}

fun restoreDatabaseFromBackup(context: Context, dbName: String, backupFile: File) {
    val dbFile = context.getDatabasePath(dbName)

    if (backupFile.exists()) {
        backupFile.inputStream().use { input ->
            dbFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}
