package com.example.musicroom

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log

class MusicService : Service() {

    private val binder = MusicBinder()
    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    fun playSong(previewUrl: String, songTitle: String, artistName: String) {
        try {
            stopSong()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(previewUrl)
                setOnPreparedListener {
                    it.start()
                    Log.d("MusicService", "Playing song: $songTitle by $artistName")
                }
                setOnCompletionListener {
                    Log.d("MusicService", "Playback completed for song: $songTitle")
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("MusicService", "Error playing song: ${e.message}")
        }
    }

    fun stopSong() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            Log.d("MusicService", "Music playback stopped")
        }
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun setOnCompletionListener(listener: () -> Unit) {
        mediaPlayer?.setOnCompletionListener {
            listener.invoke()
            Log.d("MusicService", "Song playback completed, invoking callback")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSong()
        Log.d("MusicService", "Service destroyed")
    }
}
