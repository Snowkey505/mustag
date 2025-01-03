//package com.example.mustag.data.db
//
//import androidx.room.Database
//import androidx.room.RoomDatabase
//
//@Database(
//    entities = [
//        Song::class, Artist::class, Album::class, Playlist::class, Tag::class,
//        SongArtist::class, AlbumArtist::class, SongPlaylist::class, SongTag::class
//    ],
//    version = 1
//)
//abstract class AppDatabase : RoomDatabase() {
//    abstract fun songDao(): SongDao
//    abstract fun artistDao(): ArtistDao
//    abstract fun albumDao(): AlbumDao
//    abstract fun playlistDao(): PlaylistDao
//    abstract fun tagDao(): TagDao
//}