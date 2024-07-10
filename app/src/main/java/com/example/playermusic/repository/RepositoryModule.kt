package com.example.playermusic.repository


import com.example.playermusic.domain.AlertWhatIsPlaying
import com.example.playermusic.domain.AlertWhatIsPlayingImpl
import com.example.playermusic.domain.ContentResolverMusic
import com.example.playermusic.domain.ContentResolverMusicImpl
import com.example.playermusic.data.ControllerTrack
import com.example.playermusic.data.ControllerTrackImpl
import com.example.playermusic.data.DataStoreCreate
import com.example.playermusic.data.DataStoreCreateImpl
import com.example.playermusic.data.InterfaceTransmitControllerMediaPlayer
import com.example.playermusic.domain.PathMusic
import com.example.playermusic.domain.PathMusicImpl
import com.example.playermusic.data.TransmitControllerMediaPlayer
import com.example.playermusic.domain.DataStoreSetting
import com.example.playermusic.domain.DataStoreSettingImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun provideContentResolverMusic(contentResolverMusicImpl: ContentResolverMusicImpl): ContentResolverMusic

    @Binds
    @Singleton
    abstract fun provideTransmitControllerMediaPlayer(transmitControllerMediaPlayer: TransmitControllerMediaPlayer): InterfaceTransmitControllerMediaPlayer

    @Binds
    @Singleton
    abstract fun provideControllerTrack(controllerTrackImpl: ControllerTrackImpl): ControllerTrack

    @Binds
    @Singleton
    abstract fun provideAlertWhatIsPlaying(alertWhatIsPlayingImpl: AlertWhatIsPlayingImpl): AlertWhatIsPlaying

    @Binds
    @Singleton
    abstract fun providePathMusic(pathMusicImpl: PathMusicImpl): PathMusic

    @Binds
    @Singleton
    abstract fun provideDataStoreCreaterImpl(dataStoreCreateImpl: DataStoreCreateImpl):DataStoreCreate
    @Binds
    @Singleton
    abstract fun provideDataStoreSetting(dataStoreSettingImpl: DataStoreSettingImpl): DataStoreSetting
}