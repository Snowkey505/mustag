package com.example.mustag.di

import android.content.Context
import android.content.SharedPreferences
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.MediaSession
import androidx.room.Room
import com.example.mustag.data.db.AlbumDao
import com.example.mustag.data.db.AppDatabase
import com.example.mustag.data.db.ArtistDao
import com.example.mustag.data.db.SongDao
//import androidx.room.Room
//import com.example.mustag.data.db.AlbumDao
//import com.example.mustag.data.db.AppDatabase
//import com.example.mustag.data.db.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.example.mustag.player.notification.JetAudioNotificationManager
import com.example.mustag.player.service.JetAudioServiceHandler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Provides
    @Singleton
    fun provideAudioAttributes(): AudioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @Provides
    @Singleton
    @UnstableApi
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes,
    ): ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(audioAttributes, true)
        .setHandleAudioBecomingNoisy(true)
        .setTrackSelector(DefaultTrackSelector(context))
        .build()


    @Provides
    @Singleton
    fun provideMediaSession(
        @ApplicationContext context: Context,
        player: ExoPlayer,
    ): MediaSession = MediaSession.Builder(context, player).build()

    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context,
        player: ExoPlayer,
    ): JetAudioNotificationManager = JetAudioNotificationManager(
        context = context,
        exoPlayer = player
    )

    @Provides
    @Singleton
    fun provideServiceHandler(
        exoPlayer: ExoPlayer,
        @ApplicationContext context: Context
    ): JetAudioServiceHandler {
        val sharedPreferences = context.getSharedPreferences("JetAudioPreferences", Context.MODE_PRIVATE)
        return JetAudioServiceHandler(
            exoPlayer,
            sharedPreferences = sharedPreferences
        )
    }



}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "audio_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideSongDao(database: AppDatabase): SongDao = database.songDao()

    @Provides
    fun provideAlbumDao(database: AppDatabase): AlbumDao = database.albumDao()

    @Provides
    fun provideArtistDao(database: AppDatabase): ArtistDao = database.artistDao()
}

@Module
@InstallIn(SingletonComponent::class)
object SharedPreferencesModule {
    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("jet_audio_prefs", Context.MODE_PRIVATE)
    }
}