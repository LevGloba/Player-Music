package com.example.playermusic.data

import androidx.media3.common.Player
import com.example.playermusic.ControllerMediaPlayer
import com.example.playermusic.domain.AddListenerAndRemove
import com.example.playermusic.domain.Listener
import com.example.playermusic.domain.ObserverMediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject

class TransmitControllerMediaPlayer @Inject constructor(): InterfaceTransmitControllerMediaPlayer {
    private val mutableStateFlowTransmitControllerPlayer = MutableStateFlow<ControllerMediaPlayer>(ControllerMediaPlayer.RepeatMode(Player.REPEAT_MODE_ALL))

    override fun pause() {
        mutableStateFlowTransmitControllerPlayer.update {  ControllerMediaPlayer.Pause }
    }

    override fun play() {
         mutableStateFlowTransmitControllerPlayer.update {  ControllerMediaPlayer.Play }
    }

    override fun next() {
        mutableStateFlowTransmitControllerPlayer.update { ControllerMediaPlayer.Next }
    }

    override fun previous() {
        mutableStateFlowTransmitControllerPlayer.update { ControllerMediaPlayer.Previous }
    }

    override fun seekTo(duration: Long) {
        mutableStateFlowTransmitControllerPlayer.update { ControllerMediaPlayer.SeekTo(duration) }
    }

    override fun repeatAll() {
        mutableStateFlowTransmitControllerPlayer.update { ControllerMediaPlayer.RepeatMode(Player.REPEAT_MODE_ALL) }
    }

    override fun repeatOne() {
        mutableStateFlowTransmitControllerPlayer.update { ControllerMediaPlayer.RepeatMode(Player.REPEAT_MODE_ONE) }
    }

    override fun mix() {
        mutableStateFlowTransmitControllerPlayer.update { ControllerMediaPlayer.RepeatMode(Player.REPEAT_MODE_OFF) }
    }

    override fun returnStateFlowTransMitControllerMediaPlayer(): StateFlow<ControllerMediaPlayer> = mutableStateFlowTransmitControllerPlayer.asStateFlow()
}

interface InterfaceTransmitControllerMediaPlayer {
    fun pause()
    fun play()
    fun next()
    fun previous()
    fun seekTo(duration: Long)
    fun repeatAll()
    fun repeatOne()
    fun mix()
    fun returnStateFlowTransMitControllerMediaPlayer():StateFlow<ControllerMediaPlayer>
}