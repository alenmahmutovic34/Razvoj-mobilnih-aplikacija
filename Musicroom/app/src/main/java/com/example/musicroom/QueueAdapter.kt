package com.example.musicroom.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicroom.R
import com.example.musicroom.api.Song

class QueueAdapter(private val isCreator: Boolean) : RecyclerView.Adapter<QueueAdapter.QueueViewHolder>() {

    private val queue = mutableListOf<Song>()
    private var onDeleteClickListener: ((Song) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return QueueViewHolder(view, isCreator) // Pass isCreator to the ViewHolder
    }

    override fun onBindViewHolder(holder: QueueViewHolder, position: Int) {
        val song = queue[position]
        holder.bind(song, onDeleteClickListener)
    }

    override fun getItemCount(): Int {
        return queue.size
    }

    fun updateQueue(newQueue: List<Song>, onDeleteClick: (Song) -> Unit) {
        queue.clear()
        queue.addAll(newQueue)
        this.onDeleteClickListener = onDeleteClick
        notifyDataSetChanged()
    }

    class QueueViewHolder(itemView: View, private val isCreator: Boolean) : RecyclerView.ViewHolder(itemView) {
        private val songTitleTextView: TextView = itemView.findViewById(R.id.songTitleTextView)
        private val songArtistTextView: TextView = itemView.findViewById(R.id.songArtistTextView)
        private val songVotesTextView: TextView = itemView.findViewById(R.id.songVotesTextView)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        fun bind(song: Song, onDeleteClick: ((Song) -> Unit)?) {
            songTitleTextView.text = song.title
            songArtistTextView.text = song.artist.name

            // Show votes only if greater than 1
            if (song.votes > 1) {
                songVotesTextView.visibility = View.VISIBLE
                songVotesTextView.text = "${song.votes} 👥"
            } else {
                songVotesTextView.visibility = View.GONE
            }

            // Only show delete button if the user is the creator
            deleteButton.visibility = if (isCreator) View.VISIBLE else View.GONE

            // Set up the delete button
            deleteButton.setOnClickListener {
                onDeleteClick?.invoke(song)  // Call the delete handler when clicked
            }
        }
    }
}
