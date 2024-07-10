package com.example.playermusic.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playermusic.domain.ContentResolverMusic
import com.example.playermusic.data.model.ListenerReadImpl
import com.example.playermusic.data.model.Music
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/*data class Home (
    val visible: Boolean = true,
    val musicList: List<Music> = emptyList()
)*/

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val contentResolverMusic: ContentResolverMusic
) : ViewModel() {
    val musicFlow = contentResolverMusic.getStateFlowMusic()
/*
    private val mutableStateFlow = MutableStateFlow(Home())
    val stateFlow = mutableStateFlow.asStateFlow()*/
}