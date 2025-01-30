package com.example.musicroom

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val serverUrl = "https://zavrsnirmas.onrender.com/login" // URL vašeg servera

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val usernameEditText: EditText = findViewById(R.id.usernameEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val loginButton: Button = findViewById(R.id.loginButton)
        val createAccountText: TextView = findViewById(R.id.createAccountText)
        val forgotPasswordText: TextView = findViewById(R.id.forgotPasswordText)
        val changePasswordButton: Button = findViewById(R.id.changePasswordButton)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                sendLoginRequest(username, password)
            } else {
                Toast.makeText(this, "Molimo unesite korisničko ime i lozinku!", Toast.LENGTH_SHORT).show()
            }
        }

        createAccountText.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        forgotPasswordText.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        changePasswordButton.setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun sendLoginRequest(username: String, password: String) {
        val client = OkHttpClient()
        val json = JSONObject()
        json.put("username", username)
        json.put("password", password)

        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())

        val request = Request.Builder()
            .url(serverUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("MainActivity", "Error: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Greška u konekciji sa serverom", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val message = jsonResponse.optString("message", "Prijava uspešna!")

                    // Čuvanje korisničkog imena u SharedPreferences
                    val sharedPrefs = getSharedPreferences("MusicRoomPrefs", MODE_PRIVATE)
                    sharedPrefs.edit().putString("username", username).apply()

                    runOnUiThread {
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@MainActivity, Home::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    val errorResponse = responseBody?.let { JSONObject(it).optString("error", "Prijava nije uspela") }
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, errorResponse, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}