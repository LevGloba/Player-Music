package com.example.playermusic.domain

import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class PathMusicImpl @Inject constructor(private val dataStoreSetting: DataStoreSetting): PathMusic {
    override suspend fun changePath(v: String) {
       dataStoreSetting.write(v)
    }

    override suspend fun getPath() = dataStoreSetting.read()

    override fun returnPathFlow() = dataStoreSetting.retungSettingFlow()
}

interface PathMusic {
    suspend fun changePath(v:String)
    suspend fun getPath()
    fun returnPathFlow(): StateFlow<String>
}