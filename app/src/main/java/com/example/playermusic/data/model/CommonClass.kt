package com.example.playermusic.data.model

import android.media.MediaPlayer
import com.example.playermusic.ControllerMediaPlayer
import com.example.playermusic.domain.ListenerRead
import com.example.playermusic.domain.Observer
import com.example.playermusic.domain.ObserverList
import com.example.playermusic.domain.ObserverMediaPlayer
import com.example.playermusic.domain.ObserverPlaying

class ListenerReadImpl(private val load: () -> Unit): ListenerRead {
    override fun doIt() {
        load()
    }
}

class ObserverImpl(private val listen: (MediaPlayer) -> Unit): Observer {
    override fun listener(mediaPlayer: MediaPlayer) {
        listen(mediaPlayer)
    }
}

class ObserverControllerMediaPlayer(private val listen: (ControllerMediaPlayer) -> Unit):
    ObserverMediaPlayer {
    override fun getCommand(controllerMediaPlayer: ControllerMediaPlayer) {
        listen(controllerMediaPlayer)
    }
}

class ObserverWhatIsPlaying(private val listener: (PlayingMusic) -> Unit): ObserverPlaying {
    override fun getWhatIsPLaying(v: PlayingMusic) {
        listener(v)
    }
}

class ObserveListMusic(private val listener: () -> Unit): ObserverList {
    override fun getList() {
        listener()
    }
}

