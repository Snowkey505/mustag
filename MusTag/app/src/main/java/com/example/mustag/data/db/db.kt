package com.example.mustag.data.db

import android.net.Uri
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromUri(uri: Uri): String {
        return uri.toString()
    }

    @TypeConverter
    fun toUri(uriString: String): Uri {
        return uriString.let { Uri.parse(it) }
    }
}

@Database(
    entities = [
        Song::class, Artist::class, Album::class, Playlist::class, Tag::class,
        SongArtist::class, SongPlaylist::class, SongTag::class
    ],
    version = 14
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun artistDao(): ArtistDao
    abstract fun albumDao(): AlbumDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun tagDao(): TagDao
}