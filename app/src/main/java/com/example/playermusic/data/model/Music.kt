package com.example.playermusic.data.model

import android.graphics.Bitmap

data class Music(
    val id: Long,
    val author: String,
    val preview: Bitmap? = null,
    val title: String,
    val duration: Long,
    val artist: String,
    val album: String,
    val content: String
)
