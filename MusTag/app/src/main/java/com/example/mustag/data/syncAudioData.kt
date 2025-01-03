//package com.example.mustag.data
//
//import com.example.mustag.data.db.Album
//import com.example.mustag.data.db.AlbumDao
//import com.example.mustag.data.db.Song
//import com.example.mustag.data.db.SongDao
//import com.example.mustag.data.local.ContentResolverHelper
//
//suspend fun syncAudioData(
//    helper: ContentResolverHelper,
//    songDao: SongDao,
//    albumDao: AlbumDao
//) {
//    val audioList = helper.getAudioData()
//    audioList.forEach { audio ->
//        val albumId = albumDao.insert(
//            Album(name = audio.displayName, year = 0)
//        )
//        songDao.insert(
//            Song(title = audio.title, album_id = albumId)
//        )
//    }
//}