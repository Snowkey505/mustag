package com.example.mustag.data.db

import android.net.Uri
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.time.Duration

@Entity(
    tableName = "Songs",
    foreignKeys = [
        ForeignKey(entity = Album::class, parentColumns = ["id_album"], childColumns = ["album_id"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["album_id"])]
)
data class Song(
    @PrimaryKey val id_song: Long,
    val uri: Uri,
    val displayName: String,
    val title: String,
    val duration: Int,
    val album_id: Long,
    val artwork: ByteArray?
)

@Entity(tableName = "Artists")
data class Artist(
    @PrimaryKey(autoGenerate = true) val id_artist: Long = 0,
    val name: String
)

@Entity(
    tableName = "Albums",
    foreignKeys = [
        ForeignKey(entity = Artist::class, parentColumns = ["id_artist"], childColumns = ["artist_id"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["artist_id"])]
)
data class Album(
    @PrimaryKey(autoGenerate = true) val id_album: Long = 0,
    val name: String,
    val year: Int,
    val artist_id: Long,
    val artwork: ByteArray?
)

@Entity(tableName = "Playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id_playlist: Long = 0,
    val name: String
)

@Entity(tableName = "Tags")
data class Tag(
    @PrimaryKey(autoGenerate = true) val id_tag: Long = 0,
    val name: String
)

@Entity(
    tableName = "SongArtist",
    primaryKeys = ["id_song", "id_artist"],
    foreignKeys = [
        ForeignKey(entity = Song::class, parentColumns = ["id_song"], childColumns = ["id_song"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Artist::class, parentColumns = ["id_artist"], childColumns = ["id_artist"], onDelete = ForeignKey.CASCADE)
    ]
)
data class SongArtist(
    val id_song: Long,
    val id_artist: Long
)

@Entity(
    tableName = "SongPlaylist",
    primaryKeys = ["id_song", "id_playlist"],
    foreignKeys = [
        ForeignKey(entity = Song::class, parentColumns = ["id_song"], childColumns = ["id_song"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Playlist::class, parentColumns = ["id_playlist"], childColumns = ["id_playlist"], onDelete = ForeignKey.CASCADE)
    ]
)
data class SongPlaylist(
    val id_song: Long,
    val id_playlist: Long
)

@Entity(
    tableName = "SongTag",
    primaryKeys = ["id_song", "id_tag"],
    foreignKeys = [
        ForeignKey(entity = Song::class, parentColumns = ["id_song"], childColumns = ["id_song"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Tag::class, parentColumns = ["id_tag"], childColumns = ["id_tag"], onDelete = ForeignKey.CASCADE)
    ]
)
data class SongTag(
    val id_song: Long,
    val id_tag: Long
)