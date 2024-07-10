package com.example.playermusic.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playermusic.data.InterfaceTransmitControllerMediaPlayer
import com.example.playermusic.domain.AlertWhatIsPlaying
import com.example.playermusic.domain.ContentResolverMusic
import com.example.playermusic.domain.PathMusic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val pathMusic: PathMusic,
    private val contentResolverMusic: ContentResolverMusic,
    private val alertWhatIsPlaying: AlertWhatIsPlaying,
    private val transmitControllerMediaPlayer: InterfaceTransmitControllerMediaPlayer
): ViewModel() {

    fun update() {
        viewModelScope.launch {
            contentResolverMusic.getMusicContentFromInternal()
        }
    }

    val path = pathMusic.returnPathFlow()
    val playing = alertWhatIsPlaying.returnStateFlowIsPlaying()
    val position = alertWhatIsPlaying.returnStateFlowPlayerPosition()
    val playMusic = alertWhatIsPlaying.returnStateFlowPlayerMusic()
    val mode = alertWhatIsPlaying.returnStateFlowMode()
    fun changePathMusic(v: String) {
        viewModelScope.launch{
            pathMusic.changePath(v)
            contentResolverMusic.getMusicContentFromInternal()
        }
    }

    fun next() = transmitControllerMediaPlayer.next()


    fun previous() = transmitControllerMediaPlayer.previous()


    fun play() = transmitControllerMediaPlayer.play()

    fun pause() = transmitControllerMediaPlayer.pause()

    fun repeatAll() = transmitControllerMediaPlayer.repeatAll()
    fun repeatOne() = transmitControllerMediaPlayer.repeatOne()
    fun mix() = transmitControllerMediaPlayer.mix()

    fun setPosition(v: Long) {
        alertWhatIsPlaying.setCurrentPosition(v)
    }
}