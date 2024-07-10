package com.example.playermusic.domain

import com.example.playermusic.data.ControllerTrack
import com.example.playermusic.data.InterfaceTransmitControllerMediaPlayer
import com.example.playermusic.data.model.PlayingMusic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject

class AlertWhatIsPlayingImpl @Inject constructor(
    private val transmitControllerMediaPlayer: InterfaceTransmitControllerMediaPlayer,
    private val controllerTrack: ControllerTrack
): AlertWhatIsPlaying {

    private var stateIsplaying: StateFlow<Boolean>? = null
    private var statePosition: StateFlow<Long>? = null
    private var stateMode: StateFlow<Int>? = null

    private val mutableStateFlowPayerMusic = MutableStateFlow(
        PlayingMusic(
            title = "",
            author = "",
            duration = 0
        )
    )

    override fun setMusic(playingMusic: PlayingMusic) {
        mutableStateFlowPayerMusic.update { playingMusic }
    }

    override fun playThis(v: Int) {
        controllerTrack.playThisMusic(v)
        transmitControllerMediaPlayer.play()
    }

    override fun setCurrentPosition(v: Long) {
        transmitControllerMediaPlayer.seekTo(v)
    }

    override fun returnStateFlowPlayerMusic(): StateFlow<PlayingMusic> = mutableStateFlowPayerMusic.asStateFlow()
    override fun returnStateFlowIsPlaying(): StateFlow<Boolean>? = stateIsplaying

    override fun returnStateFlowPlayerPosition(): StateFlow<Long>? = statePosition
    override fun returnStateFlowMode(): StateFlow<Int>? = stateMode
    override fun setState(pos: StateFlow<Long>, playing: StateFlow<Boolean>, mode: StateFlow<Int>) {
        stateIsplaying = playing
        statePosition = pos
        stateMode = mode
    }
}

interface AlertWhatIsPlaying {
    fun playThis(v: Int)
    fun setCurrentPosition(v: Long)
    fun setMusic(playingMusic: PlayingMusic)
    fun returnStateFlowPlayerMusic(): StateFlow<PlayingMusic>
    fun returnStateFlowIsPlaying(): StateFlow<Boolean>?
    fun returnStateFlowPlayerPosition(): StateFlow<Long>?
    fun returnStateFlowMode(): StateFlow<Int>?
    fun setState(pos:StateFlow<Long>,playing: StateFlow<Boolean>, mode: StateFlow<Int>)
}