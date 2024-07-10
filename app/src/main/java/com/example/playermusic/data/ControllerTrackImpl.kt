package com.example.playermusic.data

import com.example.playermusic.data.model.Music
import com.example.playermusic.domain.ContentResolverMusic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class ControllerTrackImpl @Inject constructor(contentResolverMusic: ContentResolverMusic): ControllerTrack {
    private val mutableStateFlowContentResolverMusic = MutableStateFlow<List<Music>>(emptyList())
    private val pairTracks: LinkedHashMap<Int, Music> = linkedMapOf()
    private var num = 0

    init {
        CoroutineScope(Dispatchers.Default).launch {
            contentResolverMusic.getStateFlowMusic().collect{ listTrack ->
            if (pairTracks != listTrack) {
                listTrack.values.forEachIndexed { index, music ->
                    pairTracks[index] = music
                }
            }
            mutableStateFlowContentResolverMusic.update {
                pairTracks.values.toList()
            }
                }
        }
    }

    override fun getIndex(): Int = num

    override fun getContent(): List<Music?> {
        return pairTracks.values.toList()
    }

    override fun playThisMusic(v: Int) {
        num = v
    }

    override fun retrunStateFlowController(): StateFlow<List<Music>> = mutableStateFlowContentResolverMusic.asStateFlow()
}

interface ControllerTrack {
    fun getIndex(): Int
    fun getContent(): List<Music?>
    fun playThisMusic(v: Int)
    fun retrunStateFlowController(): StateFlow<List<Music>>
}