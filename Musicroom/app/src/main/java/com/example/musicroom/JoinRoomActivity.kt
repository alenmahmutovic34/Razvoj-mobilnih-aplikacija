package com.example.musicroom

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class JoinRoomActivity : AppCompatActivity() {

    private val serverUrl = "https://zavrsnirmas.onrender.com/joinRoom" // IP servera

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.join_room)

        val roomCodeEditText: EditText = findViewById(R.id.roomCodeEditText)
        val joinRoomButton: Button = findViewById(R.id.joinRoomButton)
        val backToHomeButton: Button = findViewById(R.id.backToHomeButton) // Dodajte dugme za povratak na Home

        joinRoomButton.setOnClickListener {
            val roomCode = roomCodeEditText.text.toString().trim()

            if (roomCode.isEmpty()) {
                Toast.makeText(this, "Unesite kod sobe", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            joinRoom(roomCode)
        }

        // Povratak na Home ekran
        backToHomeButton.setOnClickListener {
            val intent = Intent(this, Home::class.java) // ili MainActivity
            startActivity(intent)
            finish() // Završava trenutnu aktivnost
        }
    }

    private fun joinRoom(roomCode: String) {
        val client = OkHttpClient()
        val json = JSONObject()
        val sharedPrefs = getSharedPreferences("MusicRoomPrefs", MODE_PRIVATE)
        val username = sharedPrefs.getString("username", "") ?: ""

        if (username.isEmpty()) {
            Toast.makeText(this, "Greška: Korisničko ime nije pronađeno!", Toast.LENGTH_SHORT).show()
            return
        }

        json.put("roomCode", roomCode)
        json.put("username", username) // Prosleđujemo username serveru

        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())

        val request = Request.Builder()
            .url(serverUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("JoinRoomActivity", "Error: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@JoinRoomActivity, "Greška u konekciji sa serverom", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val isCreator = jsonResponse.optBoolean("isCreator", false) // Da li je korisnik kreator?
                    val roomName = jsonResponse.optString("roomName", "Nepoznato") // Dobijamo ime sobe sa servera

                    Log.d("JoinRoomActivity", "📢 Dobijen odgovor sa servera: roomName = $roomName")

                    runOnUiThread {
                        Toast.makeText(this@JoinRoomActivity, "Uspešno ste se pridružili sobi!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@JoinRoomActivity, RoomActivity::class.java).apply {
                            putExtra("roomCode", roomCode)
                            putExtra("roomName", roomName)
                            putExtra("isCreator", isCreator)
                        }
                        startActivity(intent)
                        finish()
                    }
                } else {
                    val errorResponse = responseBody?.let { JSONObject(it).optString("error", "Došlo je do greške!") }
                    runOnUiThread {
                        if (errorResponse == "Soba je puna!") {
                            Toast.makeText(this@JoinRoomActivity, "Soba je puna!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@JoinRoomActivity, errorResponse, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }
}
