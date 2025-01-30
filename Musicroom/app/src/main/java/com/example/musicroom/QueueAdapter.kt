package com.example.musicroom.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicroom.R
import com.example.musicroom.api.Song

class QueueAdapter : RecyclerView.Adapter<QueueAdapter.QueueViewHolder>() {

    private val queue = mutableListOf<Song>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return QueueViewHolder(view)
    }

    override fun onBindViewHolder(holder: QueueViewHolder, position: Int) {
        val song = queue[position]
        holder.bind(song)
    }

    override fun getItemCount(): Int {
        return queue.size
    }

    fun updateQueue(newQueue: List<Song>) {
        queue.clear()
        // Sortiramo pjesme po broju glasova u opadajućem redoslijedu
        queue.addAll(newQueue.sortedByDescending { it.votes })
        notifyDataSetChanged()
    }

    class QueueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val songTitleTextView: TextView = itemView.findViewById(R.id.songTitleTextView)
        private val songArtistTextView: TextView = itemView.findViewById(R.id.songArtistTextView)
        private val songVotesTextView: TextView = itemView.findViewById(R.id.songVotesTextView)

        fun bind(song: Song) {
            songTitleTextView.text = song.title
            songArtistTextView.text = song.artist.name

            // Prikazujemo broj glasova samo ako je veći od 1
            if (song.votes > 1) {
                songVotesTextView.visibility = View.VISIBLE
                songVotesTextView.text = "${song.votes} 👥" // Koristimo emoji kao ikonu za glasove
            } else {
                songVotesTextView.visibility = View.GONE
            }
        }
    }
}