package com.example.musicroom

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException
import java.util.UUID

class CreateRoomActivity : AppCompatActivity() {
    private val serverUrl = "https://zavrsnirmas.onrender.com/createRoom"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_room)

        val roomNameEditText = findViewById<EditText>(R.id.roomNameEditText)
        val maxUsersEditText = findViewById<EditText>(R.id.maxUsersEditText)
        val createRoomButton = findViewById<Button>(R.id.createRoomButton)

        // Get username from SharedPreferences
        val sharedPrefs = getSharedPreferences("MusicRoomPrefs", MODE_PRIVATE)
        val username = sharedPrefs.getString("username", "") ?: ""

        createRoomButton.setOnClickListener {
            val roomName = roomNameEditText.text.toString().trim()
            val maxUsers = maxUsersEditText.text.toString().trim()

            if (roomName.isEmpty() || maxUsers.isEmpty()) {
                Toast.makeText(this, "Molimo popunite sva polja!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (username.isEmpty()) {
                Toast.makeText(this, "Korisnik nije prijavljen!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val roomCode = UUID.randomUUID().toString().substring(0, 8)

            val json = JSONObject().apply {
                put("room_name", roomName)
                put("max_users", maxUsers.toInt())
                put("room_code", roomCode)
                put("username", username)
            }

            sendCreateRoomRequest(json)
        }
    }

    private fun sendCreateRoomRequest(json: JSONObject) {
        val client = OkHttpClient()
        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())

        val request = Request.Builder()
            .url(serverUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("CreateRoomActivity", "Error: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@CreateRoomActivity, "Greška pri kreiranju sobe!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val message = jsonResponse.optString("message", "Soba uspešno kreirana!")
                    val roomCode = jsonResponse.optString("room_code")
                    val roomName = jsonResponse.optString("room_name")
                    val roomUsername = jsonResponse.optString("username")

                    runOnUiThread {
                        Toast.makeText(this@CreateRoomActivity, message, Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@CreateRoomActivity, RoomActivity::class.java).apply {
                            putExtra("roomCode", roomCode)
                            putExtra("roomName", roomName)
                            putExtra("username", roomUsername)
                            putExtra("isCreator", true) // ✅ Ovim kažemo RoomActivity-ju da je kreator
                        }
                        startActivity(intent)
                        finish()
                    }

                } else {
                    runOnUiThread {
                        Toast.makeText(this@CreateRoomActivity, "Došlo je do greške!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}