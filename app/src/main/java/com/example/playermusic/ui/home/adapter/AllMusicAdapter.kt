package com.example.playermusic.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.playermusic.R
import com.example.playermusic.data.model.Music
import com.example.playermusic.databinding.ItemViewMusicBinding
import com.google.android.material.snackbar.Snackbar

class AllMusicAdapter(
    private val listener: (Int) -> Unit,
    @DrawableRes private val defaultPreview: Int = R.drawable.icon
): ListAdapter<Music, AllMusicAdapter.ItemViewHolderMusic>(
    capsulesListDiffUtilCallBack) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolderMusic {
        val itemViewMusicBinding = ItemViewMusicBinding.inflate(LayoutInflater.from(parent.context))
        return ItemViewHolderMusic(itemViewMusicBinding)
    }

    override fun onBindViewHolder(holder: ItemViewHolderMusic, position: Int) {
        holder.initItemView(getItem(position))
    }

    inner class ItemViewHolderMusic(private val binding: ItemViewMusicBinding): RecyclerView.ViewHolder(binding.root) {
        fun initItemView(v: Music) {
            binding.run {
                v.run {
                    if (preview != null)
                        imageViewIconMusic.setImageBitmap(v.preview)
                    else
                        imageViewIconMusic.setImageResource(defaultPreview)
                    textViewSong.text = v.title
                    textViewAuthor.text = v.artist
                }
                constraintLayoutNavHeaderMain.setOnLongClickListener {
                    Snackbar.make(root,R.string.search_song,Snackbar.LENGTH_LONG).show()
                    true
                }
                constraintLayoutNavHeaderMain.setOnClickListener { listener(adapterPosition) }
            }
        }
    }
}

private val capsulesListDiffUtilCallBack = object : DiffUtil.ItemCallback<Music>() {
    override fun areItemsTheSame(oldItem: Music, newItem: Music): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Music, newItem: Music): Boolean =
        oldItem == newItem
}