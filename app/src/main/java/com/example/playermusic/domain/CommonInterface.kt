package com.example.playermusic.domain

import android.media.MediaPlayer
import com.example.playermusic.ControllerMediaPlayer
import com.example.playermusic.data.model.PlayingMusic

interface Listener

interface AddListenerAndRemove {
    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}

interface Observer: Listener {
    fun listener(mediaPlayer: MediaPlayer)
}

interface ListenerRead: Listener {
    fun doIt()
}

interface ObserverMediaPlayer: Listener {
    fun getCommand(controllerMediaPlayer: ControllerMediaPlayer)
}
interface ObserverPlaying: Listener {
    fun getWhatIsPLaying(v: PlayingMusic)
}
interface ObserverList: Listener {
    fun getList()
}
interface Read {
   suspend fun read()
}

interface Write {
    suspend fun write(v: String)
}

interface ReadAndWrite: Read,Write
