package com.example.playermusic.domain

import android.content.ContentUris
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.provider.MediaStore.Audio.Media
import android.util.Log
import com.example.playermusic.data.ControllerTrack
import com.example.playermusic.data.model.Music
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject

class ContentResolverMusicImpl @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val pathMusic: PathMusic
): ContentResolverMusic {

    private val mutableStateFlowMusic = MutableStateFlow<LinkedHashMap<Long,Music>>(linkedMapOf())
    private val pairTracks: LinkedHashMap<Long,Music> = linkedMapOf()

    private val projection = arrayOf(
        Media._ID,
        Media.TITLE,
        Media.DURATION,
        Media.ARTIST,
        Media.AUTHOR,
        Media.ALBUM,
        Media.DATA
    )

    private val metadataRetriever = MediaMetadataRetriever()

    override fun getStateFlowMusic(): StateFlow<LinkedHashMap<Long, Music>> = mutableStateFlowMusic.asStateFlow()

    override suspend fun getMusicContentFromInternal() {
        pathMusic.getPath()
        pathMusic.returnPathFlow().collect {
            val select =
                Media.IS_MUSIC + " != 0 AND " + Media.DATA + " LIKE'" + it + "/%'"
            applicationContext.contentResolver.query(
                Media.EXTERNAL_CONTENT_URI,
                projection,
                select,
                null,
                null
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(Media._ID)
                val artistId = cursor.getColumnIndexOrThrow(Media.ARTIST)
                val nameId = cursor.getColumnIndexOrThrow(Media.TITLE)
                val durationId = cursor.getColumnIndexOrThrow(Media.DURATION)
                val authorId = cursor.getColumnIndexOrThrow(Media.AUTHOR)
                val albumId = cursor.getColumnIndexOrThrow(Media.ALBUM)
                val data = cursor.getColumnIndexOrThrow(Media.DATA)
                Log.e("load", "search")
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val artist = cursor.getString(artistId)
                    val name = cursor.getString(nameId)
                    val duration = cursor.getInt(durationId)
                    val album = cursor.getString(albumId)
                    val author = cursor.getString(authorId)
                    val path = cursor.getString(data)
                    metadataRetriever.setDataSource(path)
                    val art = metadataRetriever.embeddedPicture
                    val opt = BitmapFactory.Options()
                    opt.inSampleSize = 2
                    val img = art?.let { BitmapFactory.decodeByteArray(art, 0, it.size, opt) }
                    pairTracks.put(
                        id, Music(
                            id = id,
                            duration = duration.toLong(),
                            title = name.orEmpty(),
                            author = author.orEmpty(),
                            preview = img,
                            album = album ?: author,
                            artist = artist ?: album,
                            content = ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, id)
                                .toString()
                        )
                    )
                }
                cursor.close()
                Log.e("load", "search_end")
            }
            Log.e("load", "call")
            mutableStateFlowMusic.update {
                pairTracks
            }
        }
    }

    override fun returnSongs(): HashMap<Long, Music> {
        return pairTracks
    }
}

interface ContentResolverMusic {
    fun returnSongs(): HashMap<Long, Music>
    suspend fun getMusicContentFromInternal()
    fun getStateFlowMusic(): StateFlow<LinkedHashMap<Long,Music>>
}
