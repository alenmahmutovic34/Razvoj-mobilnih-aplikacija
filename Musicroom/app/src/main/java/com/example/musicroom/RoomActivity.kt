package com.example.musicroom

import android.content.*
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicroom.adapters.QueueAdapter
import com.example.musicroom.adapters.SongSearchAdapter
import com.example.musicroom.adapters.UserAdapter
import com.example.musicroom.api.Album
import com.example.musicroom.api.Artist
import com.example.musicroom.api.DeezerApiService
import com.example.musicroom.api.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class RoomActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var deezerApiService: DeezerApiService
    private lateinit var songSearchAdapter: SongSearchAdapter
    private lateinit var queueAdapter: QueueAdapter
    private lateinit var userAdapter: UserAdapter
    private val playbackQueue = mutableListOf<Song>()
    private val userList = mutableListOf<String>()
    private var musicService: MusicService? = null
    private var isServiceBound = false
    private var roomCode: String? = null
    private var roomName: String? = null
    private var username: String? = null
    private var webSocket: WebSocket? = null
    private var currentlyPlayingTextView: TextView? = null
    private val addedSongs = mutableSetOf<String>()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? MusicService.MusicBinder
            musicService = binder?.getService()
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("RoomActivity", "📢 Učitano iz Intent-a: roomName = $roomName, roomCode = $roomCode")

        super.onCreate(savedInstanceState)

        val isCreator = intent.getBooleanExtra("isCreator", false)
        username = intent.getStringExtra("username")
        roomCode = intent.getStringExtra("roomCode") ?: "Nepoznato"

        if (username.isNullOrBlank()) {
            Log.e("RoomActivity", "❌ Greška: Username nije dobijen iz Intent-a!")
        } else {
            Log.d("RoomActivity", "✅ Username učitan: $username")
        }

        if (roomCode == "Nepoznato") {
            Log.e("RoomActivity", "❌ Greška: RoomCode nije postavljen!")
        } else {
            Log.d("RoomActivity", "✅ RoomCode učitan: $roomCode")
        }

        if (isCreator) {
            setContentView(R.layout.activity_room)
        } else {
            setContentView(R.layout.guest_activity_room)
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.deezer.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        deezerApiService = retrofit.create(DeezerApiService::class.java)

        roomCode = intent.getStringExtra("roomCode") ?: "Nepoznato"
        roomName = intent.getStringExtra("roomName") ?: "Nepoznato"

        // UI Elements initialization
        val roomInfoTextView: TextView = findViewById(R.id.roomNameTextView)
        val songListRecyclerView: RecyclerView = findViewById(R.id.songListRecyclerView)
        val queueRecyclerView: RecyclerView = findViewById(R.id.queueRecyclerView)
        val userListRecyclerView: RecyclerView = findViewById(R.id.userListRecyclerView)
        val exitRoomButton: Button = findViewById(R.id.exitRoomButton)
        val songSearchInput: EditText? = findViewById(R.id.songInputEditText)
        val searchSongButton: Button? = findViewById(R.id.searchSongButton)
        currentlyPlayingTextView = findViewById(R.id.currentlyPlayingTextView)

        roomInfoTextView.text = "Soba: $roomName\nKod sobe: $roomCode"

        // RecyclerViews setup
        songListRecyclerView.layoutManager = LinearLayoutManager(this)
        queueRecyclerView.layoutManager = LinearLayoutManager(this)
        userListRecyclerView.layoutManager = LinearLayoutManager(this)

        songSearchAdapter = SongSearchAdapter { song ->
            val songKey = "${song.title}-${song.artist.name}"
            if(songKey in addedSongs) {
                Toast.makeText(
                    this,
                    "Vec ste dodali ovu pjesmu u red za slusanje", Toast.LENGTH_SHORT).show()
            } else {
                addSongToQueue(song)
                addedSongs.add(songKey)
            }
        }
        queueAdapter = QueueAdapter(isCreator)
        userAdapter = UserAdapter(userList)

        songListRecyclerView.adapter = songSearchAdapter
        queueRecyclerView.adapter = queueAdapter
        userListRecyclerView.adapter = userAdapter

        // Dodajemo TextWatcher za real-time pretragu
        songSearchInput?.addTextChangedListener(object : TextWatcher {
            private var searchJob: Job? = null

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()

                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(300)
                    val query = s?.toString()?.trim() ?: ""
                    if (query.isNotEmpty()) {
                        searchSongs(query)
                    } else {
                        songSearchAdapter.updateSongs(emptyList())
                    }
                }
            }
        })

        // Sakrivamo dugme za pretragu jer sada imamo real-time pretragu
        searchSongButton?.visibility = View.GONE

        if (isCreator) {
            val playButton: Button = findViewById(R.id.playButton)
            val stopButton: Button = findViewById(R.id.stopButton)

            playButton.setOnClickListener { playNextSong() }
            stopButton.setOnClickListener {
                musicService?.stopSong()
                Toast.makeText(this, "Reprodukcija zaustavljena", Toast.LENGTH_SHORT).show()

                val stopMessage = JSONObject().apply {
                    put("type", "stopSong")
                    put("roomCode", roomCode)
                }

                webSocket?.send(stopMessage.toString())
            }
        }

        exitRoomButton.setOnClickListener {
            if (username.isNullOrBlank()) {
                Log.e("RoomActivity", "❌ Greška: Username nije postavljen prilikom izlaska!")
            } else {
                val leaveMessage = JSONObject().apply {
                    put("type", "leaveRoom")
                    put("roomCode", roomCode)
                    put("username", username)  // Pobrinimo se da username nije null
                }
                webSocket?.send(leaveMessage.toString())
            }

            webSocket?.close(1000, null)

            val intent = Intent(this, Home::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
            finish()
        }

        connectToWebSocket()

        if (isCreator) {
            val intent = Intent(this, MusicService::class.java)
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun connectToWebSocket() {
        val client = OkHttpClient()
        val request = Request.Builder().url("wss://zavrsnirmas.onrender.com").build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                val message = JSONObject(text)
                when (message.getString("type")) {
                    "roomJoined" -> {
                        val songs = message.getJSONArray("songs")
                        val updatedSongs = parseSongsFromJsonArray(songs)
                        runOnUiThread {
                            playbackQueue.clear()
                            playbackQueue.addAll(updatedSongs.sortedByDescending { it.votes })
                            queueAdapter.updateQueue(playbackQueue, ::removeSongFromQueue)

                            addedSongs.clear()
                            updatedSongs.forEach { song ->
                                val songKey = "${song.title}-${song.artist.name}"
                                addedSongs.add(songKey)
                            }
                        }
                    }

                    "updateQueue" -> {
                        val songs = message.getJSONArray("songs")
                        val updatedSongs = parseSongsFromJsonArray(songs)
                        runOnUiThread {
                            playbackQueue.clear()
                            playbackQueue.addAll(updatedSongs.sortedByDescending { it.votes })
                            queueAdapter.updateQueue(playbackQueue, ::removeSongFromQueue)
                        }
                    }

                    "updateUsers" -> {
                        val usersJsonArray = message.getJSONArray("users")
                        val updatedUsers = mutableListOf<String>()

                        for (i in 0 until usersJsonArray.length()) {
                            val user = usersJsonArray.getString(i)
                            if (!user.isNullOrBlank() && user != "null") {  // Filtriramo null vrednosti
                                updatedUsers.add(user)
                            }
                        }

                        runOnUiThread {
                            Log.d("RoomActivity", "📋 Ažurirana lista korisnika: $updatedUsers")
                            userList.clear()
                            userList.addAll(updatedUsers)
                            userAdapter.notifyDataSetChanged()
                        }



                    }
                    "currentlyPlaying" -> {
                        val songJson = message.optJSONObject("song")
                        runOnUiThread {
                            if (songJson != null) {
                                val song = parseSongFromJson(songJson)
                                currentlyPlayingTextView?.text = "Trenutno svira: ${song.title} - ${song.artist.name}"

                                val songKey = "${song.title}-${song.artist.name}"
                                addedSongs.remove(songKey)
                            } else {
                                currentlyPlayingTextView?.text = "Trenutno svira: None"
                            }
                        }
                    }
                    "songPlayed" -> {
                        val songKey = message.getString("songKey")
                        runOnUiThread {
                            addedSongs.remove(songKey)
                        }
                    }

                }
            }
        })

        roomCode?.let {
            val joinMessage = JSONObject().apply {
                put("type", "joinRoom")
                put("roomCode", it)
                put("username", username)
            }
            webSocket?.send(joinMessage.toString())
        }
    }

    // Existing methods...
    private fun searchSongs(query: String) {
        Log.d("RoomActivity", "searchSongs() je pokrenut sa upitom: $query")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = deezerApiService.searchSongs(query).execute()

                if (response.isSuccessful) {
                    val songs = response.body()?.data ?: emptyList()
                    Log.d("RoomActivity", "Pronađeno pesama: ${songs.size}")
                    runOnUiThread {
                        if (songs.isNotEmpty()) {
                            songSearchAdapter.updateSongs(songs)
                        } else {
                            Toast.makeText(
                                this@RoomActivity,
                                "Nema rezultata za: $query",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Log.e("RoomActivity", "Neuspela pretraga! HTTP kod: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("RoomActivity", "Izuzetak tokom pretrage: ${e.message}", e)
            }
        }
    }

    private fun addSongToQueue(song: Song) {
        val songMessage = JSONObject().apply {
            put("type", "addSong")
            put("roomCode", roomCode)
            put("song", JSONObject().apply {
                put("title", song.title)
                put("artist", song.artist.name)
                put("album", song.album.title)
                put("preview", song.preview)
            })
        }
        webSocket?.send(songMessage.toString())
    }

    private fun playNextSong() {
        if (playbackQueue.isNotEmpty()) {
            val nextSong = playbackQueue.removeAt(0)
            queueAdapter.updateQueue(playbackQueue, ::removeSongFromQueue)
            musicService?.playSong(nextSong.preview, nextSong.title, nextSong.artist.name)

            val songKey = "${nextSong.title}-${nextSong.artist.name}"
            addedSongs.remove(songKey)

            val playMessage = JSONObject().apply {
                put("type", "playSong")
                put("roomCode", roomCode)
                put("song", JSONObject().apply {
                    put("title", nextSong.title)
                    put("artist", nextSong.artist.name)
                    put("album", nextSong.album.title)
                    put("preview", nextSong.preview)
                })
            }
            webSocket?.send(playMessage.toString())
        } else {
            Toast.makeText(this, "Red za puštanje je prazan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeSongFromQueue(song: Song) {
        playbackQueue.remove(song)
        queueAdapter.updateQueue(playbackQueue, ::removeSongFromQueue)

        val songKey = "${song.title}-${song.artist.name}"
        addedSongs.remove(songKey)

        val removeMessage = JSONObject().apply {
            put("type", "removeSong")
            put("roomCode", roomCode)
            put("song", JSONObject().apply {
                put("title", song.title)
                put("artist", song.artist.name)
                put("album", song.album.title)
                put("preview", song.preview)
            })
        }
        webSocket?.send(removeMessage.toString())
    }

    private fun parseSongsFromJsonArray(jsonArray: JSONArray): List<Song> {
        val songs = mutableListOf<Song>()
        for (i in 0 until jsonArray.length()) {
            val songJson = jsonArray.getJSONObject(i)
            songs.add(parseSongFromJson(songJson))
        }
        return songs
    }

    private fun parseSongFromJson(songJson: JSONObject): Song {
        return Song(
            title = songJson.getString("title"),
            artist = Artist(songJson.getString("artist")),
            album = Album(songJson.getString("album")),
            preview = songJson.getString("preview"),
            votes = songJson.optInt("votes", 1)
        )
    }

    public override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    public override fun onResume() {
        super.onResume()
        if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
            mediaPlayer?.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (username != null && roomCode != null) {
            val leaveMessage = JSONObject().apply {
                put("type", "leaveRoom")
                put("roomCode", roomCode)
                put("username", username)
            }
            webSocket?.send(leaveMessage.toString())
        }

        webSocket?.close(1000, null)

        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }

}