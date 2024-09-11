package com.example.musicplayer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.databinding.ItemMusicBinding

class MusicAdapter(
    private val musicList: List<AudioModel>,
    private val onItemClick: (AudioModel) -> Unit
) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    inner class MusicViewHolder(private val binding: ItemMusicBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(audioModel: AudioModel) {
            binding.songTitle.text = audioModel.title
            binding.artistName.text = audioModel.artist
            binding.root.setOnClickListener { onItemClick(audioModel) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val binding = ItemMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MusicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        holder.bind(musicList[position])
    }

    override fun getItemCount() = musicList.size
}
