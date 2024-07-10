package com.example.playermusic

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC
import androidx.media3.common.C.USAGE_MEDIA
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import com.example.playermusic.domain.AlertWhatIsPlaying
import com.example.playermusic.data.ControllerTrack
import com.example.playermusic.data.InterfaceTransmitControllerMediaPlayer
import com.example.playermusic.data.model.ObserveListMusic
import com.example.playermusic.data.model.ObserverControllerMediaPlayer
import com.example.playermusic.data.model.PlayingMusic
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
private const val NOTIFICATION_ID = 1
@AndroidEntryPoint
class MediaPlayerService: MediaSessionService(), Player.Listener, MediaSession.Callback {

    private val mutableStateFlowPlayingCurrentPostion = MutableStateFlow(0L)
    private val mutableStateFlowPlayingCurrentIsPlaying = MutableStateFlow(false)
    private val mutableStateFlowMode = MutableStateFlow(REPEAT_MODE_ALL)

    private val stateMode = mutableStateFlowMode.asStateFlow()
    private val positionState = mutableStateFlowPlayingCurrentPostion.asStateFlow()
    private val isPlayingState = mutableStateFlowPlayingCurrentIsPlaying.asStateFlow()

    private var mediaSession: MediaSession? = null
    @Inject
    lateinit var transmitControllerMediaPlayer: InterfaceTransmitControllerMediaPlayer
    @Inject
    lateinit var controllerTrack: ControllerTrack
    @Inject
    lateinit var alertWhatIsPlaying: AlertWhatIsPlaying

    override fun onCreate() {
        super.onCreate()
        alertWhatIsPlaying.setState(positionState,isPlayingState, stateMode)
        val player = ExoPlayer.Builder(this).apply {
            setAudioAttributes(AudioAttributes.Builder().setContentType(AUDIO_CONTENT_TYPE_MUSIC).setUsage(USAGE_MEDIA).build(),true)
        }.build()
        player.addListener(this)
        mediaSession = MediaSession.Builder(this, player).build()
        loadMusic()
        eventListenerPlayer()
    }

    private fun repeatMode(mode: Int) {
        mediaSession?.player?.run {
            if (REPEAT_MODE_OFF == mode) {
                shuffleModeEnabled = true
                repeatMode = REPEAT_MODE_ALL
            }
            else {
                shuffleModeEnabled = false
                repeatMode = mode
            }
            mutableStateFlowMode.update {
                mode
            }
        }
    }

    private val coroutineService = CoroutineScope(Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>
    ): ListenableFuture<MutableList<MediaItem>> {
        val updatedMediaItems = mediaItems.map { it.buildUpon().setUri(it.mediaId).build() }.toMutableList()
        return Futures.immediateFuture(updatedMediaItems)
    }

    private fun seekTo(duration: Long) {
        mediaSession?.player?.seekTo(duration * 100)
    }

    private fun loadMusic() {
        MainScope().launch {
            controllerTrack.retrunStateFlowController().collect { listMusic ->
                val mutableMediaItem = mutableListOf<MediaItem>()
                listMusic.forEach {
                    val mediaItem = MediaItem.Builder()
                        .setMediaId("${it.id}")
                        .setUri(it.content)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setArtist(it.artist)
                                .setTitle(it.title)
                                .setArtworkUri(it.content?.toUri())
                                .build()
                        ).build()
                    mutableMediaItem.add(mediaItem)
                }
                mediaSession?.player?.run {
                    setMediaItems(mutableMediaItem)
                    prepare()
                }
                if (listMusic.isNotEmpty())
                    sendData(controllerTrack.getIndex())
            }
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        coroutineService.cancel()
        super.onDestroy()
    }

    @OptIn(UnstableApi::class)
    private fun eventListenerPlayer() {
        MainScope().launch {
            transmitControllerMediaPlayer.returnStateFlowTransMitControllerMediaPlayer().collect { controllerMediaPlayer ->
                when (controllerMediaPlayer) {
                    ControllerMediaPlayer.Pause -> mediaSession?.player?.pause()
                    ControllerMediaPlayer.Play -> {
                        mediaSession?.player?.let {
                            val index = controllerTrack.getIndex()
                            if (index != it.currentMediaItemIndex)
                                it.seekTo(index, 0)
                            it.play()
                        }
                    }

                    ControllerMediaPlayer.Next -> mediaSession?.player?.next()
                    ControllerMediaPlayer.Previous -> {
                        if ((mediaSession?.player?.currentPosition
                                ?: 0) < ((mediaSession?.player?.duration?.times(0.15))?.toInt()
                                ?: 0)
                        )
                            mediaSession?.player?.previous()
                        else
                            mediaSession?.player?.let {
                                seekTo(-it.currentPosition)
                            }
                    }

                    is ControllerMediaPlayer.SeekTo -> seekTo(controllerMediaPlayer.duration)
                    ControllerMediaPlayer.Load -> loadMusic()
                    is ControllerMediaPlayer.RepeatMode -> repeatMode(mode = controllerMediaPlayer.mode)
                }
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        mutableStateFlowPlayingCurrentIsPlaying.update {
            isPlaying
        }
        mediaSession?.player?.let { sendData(it.currentMediaItemIndex, isPlaying) }
    }

    private fun sendData(index: Int, isPlaying: Boolean = false) {
        mediaSession?.player?.run {
            getMediaItemAt(index).mediaMetadata.let { music ->
                val playerMusic = PlayingMusic(
                    title = music.title.toString(),
                    author = music.artist.toString(),
                    duration = mediaSession!!.player.duration,
                )
                alertWhatIsPlaying.setMusic(playerMusic)
                coroutineService.launch(Dispatchers.Main){
                    while (isPlaying) {
                        mutableStateFlowPlayingCurrentPostion.update {
                            mediaSession!!.player.currentPosition
                        }
                        delay(50)
                    }
                }
            }
        }
    }
}

sealed class ControllerMediaPlayer {
    data object Pause: ControllerMediaPlayer()
    data object Play: ControllerMediaPlayer()
    data object Next: ControllerMediaPlayer()
    data object Previous: ControllerMediaPlayer()
    data class SeekTo(val duration: Long): ControllerMediaPlayer()
    data object Load: ControllerMediaPlayer()
    data class RepeatMode(val mode: Int): ControllerMediaPlayer()
}