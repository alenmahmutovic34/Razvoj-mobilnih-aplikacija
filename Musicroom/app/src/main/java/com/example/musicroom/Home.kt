package com.example.musicroom

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Home : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Preuzimanje username-a iz SharedPreferences
        val sharedPrefs = getSharedPreferences("MusicRoomPrefs", Context.MODE_PRIVATE)
        val username = sharedPrefs.getString("username", "User") // Default ako nema username-a

        // Postavljanje teksta u TextView
        val welcomeTextView: TextView = findViewById(R.id.welcome_text)
        welcomeTextView.text = "WELCOME BACK, $username!"

        val createRoomButton: Button = findViewById(R.id.create_room_button)
        createRoomButton.setOnClickListener {
            val intent = Intent(this, CreateRoomActivity::class.java)
            startActivity(intent)
        }

        val joinRoomButton: Button = findViewById(R.id.join_room_button)
        joinRoomButton.setOnClickListener {
            val intent = Intent(this, JoinRoomActivity::class.java)
            startActivity(intent)
        }

        // Logout dugme - brisanje username-a i povratak na login ekran
        val logoutButton: Button = findViewById(R.id.logout_button)
        logoutButton.setOnClickListener {
            val editor = sharedPrefs.edit()
            editor.remove("username") // Briše username da korisnik mora ponovo da se uloguje
            editor.apply()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Sprečava povratak
            startActivity(intent)
        }
    }
}
