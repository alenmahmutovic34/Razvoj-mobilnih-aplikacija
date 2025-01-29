package com.example.musicroom.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicroom.R
import com.example.musicroom.api.Song

class SongSearchAdapter(private val onSongClick: (Song) -> Unit) :
    RecyclerView.Adapter<SongSearchAdapter.SongViewHolder>() {

    private val songs = mutableListOf<Song>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.bind(song)
        holder.itemView.setOnClickListener {
            onSongClick(song)
        }
    }

    override fun getItemCount(): Int = songs.size

    fun updateSongs(newSongs: List<Song>) {
        songs.clear()
        songs.addAll(newSongs)
        notifyDataSetChanged()
    }

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val songTitleTextView: TextView = itemView.findViewById(R.id.songTitleTextView)
        private val songArtistTextView: TextView = itemView.findViewById(R.id.songArtistTextView)

        fun bind(song: Song) {
            songTitleTextView.text = song.title  // Prikazuje naslov pesme
            songArtistTextView.text = song.artist.name  // Prikazuje ime izvođača
        }
    }
}
