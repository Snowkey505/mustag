package com.example.mustag.data.db

//import androidx.room.Dao
//import androidx.room.Delete
//import androidx.room.Insert
//import androidx.room.Query
//import androidx.room.Update
//
//@Dao
//interface SongDao {
//    @Insert
//    suspend fun insert(song: Song): Long
//
//    @Query("SELECT * FROM Songs")
//    suspend fun getAllSongs(): List<Song>
//
//    @Query("SELECT * FROM Songs WHERE id_song = :id")
//    suspend fun getSongById(id: Long): Song?
//
//    @Query("SELECT * FROM Songs WHERE album_id = :albumId")
//    suspend fun getSongsByAlbum(albumId: Long): List<Song>
//
//    @Query("SELECT Songs.* FROM Songs INNER JOIN SongArtist ON Songs.id_song = SongArtist.id_song WHERE SongArtist.id_artist = :artistId")
//    suspend fun getSongsByArtist(artistId: Long): List<Song>
//
//    @Query("SELECT Songs.* FROM Songs INNER JOIN SongPlaylist ON Songs.id_song = SongPlaylist.id_song WHERE SongPlaylist.id_playlist = :playlistId")
//    suspend fun getSongsByPlaylist(playlistId: Long): List<Song>
//
//    @Query("SELECT Songs.* FROM Songs INNER JOIN SongTag ON Songs.id_song = SongTag.id_song WHERE SongTag.id_tag = :tagId")
//    suspend fun getSongsByTag(tagId: Long): List<Song>
//
//    @Delete
//    suspend fun delete(song: Song)
//
//    @Update
//    suspend fun update(song: Song)
//}
//
//@Dao
//interface ArtistDao {
//    @Insert
//    suspend fun insert(artist: Artist): Long
//
//    @Query("SELECT * FROM Artists WHERE id_artist = :id")
//    suspend fun getArtistById(id: Long): Artist?
//
//    @Query("SELECT Artists.* FROM Artists INNER JOIN AlbumArtist ON Artists.id_artist = AlbumArtist.id_artist WHERE AlbumArtist.id_album = :albumId")
//    suspend fun getArtistsByAlbum(albumId: Long): List<Artist>
//
//    @Query("SELECT Artists.* FROM Artists INNER JOIN SongArtist ON Artists.id_artist = SongArtist.id_artist WHERE SongArtist.id_song = :songId")
//    suspend fun getArtistBySong(songId: Long): Artist
//
//    @Delete
//    suspend fun delete(artist: Artist)
//
//    @Update
//    suspend fun update(artist: Artist)
//}
//
//@Dao
//interface AlbumDao {
//    @Insert
//    suspend fun insert(album: Album): Long
//
//    @Query("SELECT * FROM Albums WHERE id_album = :id")
//    suspend fun getAlbumById(id: Long): Album?
//
//    @Query("SELECT Albums.* FROM Albums INNER JOIN Songs ON Songs.album_id = Albums.id_album WHERE Songs.id_song = :songId")
//    suspend fun getAlbumBySong(songId: Long): Album?
//
//    @Delete
//    suspend fun delete(album: Album)
//
//    @Update
//    suspend fun update(album: Album)
//}
//
//@Dao
//interface PlaylistDao {
//    @Insert
//    suspend fun insert(playlist: Playlist): Long
//
//    @Query("SELECT * FROM Playlists WHERE id_playlist = :id")
//    suspend fun getPlaylistById(id: Long): Playlist?
//
//    @Query("SELECT Playlists.* FROM Playlists INNER JOIN SongPlaylist ON Playlists.id_playlist = SongPlaylist.id_playlist WHERE SongPlaylist.id_song = :songId")
//    suspend fun getPlaylistsBySong(songId: Long): List<Playlist>
//
//    @Delete
//    suspend fun delete(playlist: Playlist)
//
//    @Update
//    suspend fun update(playlist: Playlist)
//}
//
//@Dao
//interface TagDao {
//    @Insert
//    suspend fun insert(tag: Tag): Long
//
//    @Query("SELECT * FROM Tags WHERE id_tag = :id")
//    suspend fun getTagById(id: Long): Tag?
//
//    @Query("SELECT Tags.* FROM Tags INNER JOIN SongTag ON Tags.id_tag = SongTag.id_tag WHERE SongTag.id_song = :songId")
//    suspend fun getTagsBySong(songId: Long): List<Tag>
//
//    @Delete
//    suspend fun delete(tag: Tag)
//
//    @Update
//    suspend fun update(tag: Tag)
//}