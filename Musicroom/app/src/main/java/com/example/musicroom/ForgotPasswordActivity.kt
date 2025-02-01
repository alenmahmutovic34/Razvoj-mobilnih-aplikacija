// ForgotPasswordActivity.kt
package com.example.musicroom

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class ForgotPasswordActivity : AppCompatActivity() {
    private val serverUrl = "https://zavrsnirmas.onrender.com/forgotPassword" // Endpoint na serveru za zaboravljenu lozinku

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgot_password_activity)

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val sendButton = findViewById<Button>(R.id.sendButton)
        val backToLoginButton = findViewById<Button>(R.id.backToLoginButton)

        sendButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            println("Kliknuto na sendButton sa emailom: $email") // Dodato za debug

            if (email.isEmpty()) {
                Toast.makeText(this, "Molimo unesite email adresu!", Toast.LENGTH_SHORT).show()
            } else {
                sendForgotPasswordRequest(email)
            }
        }

        backToLoginButton.setOnClickListener {
            finish() // Vrati se na prethodnu aktivnost
        }
    }

    private fun sendForgotPasswordRequest(email: String) {
        val client = OkHttpClient()
        val json = JSONObject()
        json.put("email", email)

        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())

        val request = Request.Builder()
            .url(serverUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Greška u konekciji sa serverom: ${e.message}") // Dodato za debug
                runOnUiThread {
                    Toast.makeText(this@ForgotPasswordActivity, "Greška u konekciji sa serverom", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                println("Odgovor servera: $responseBody") // Dodato za debug

                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val message = jsonResponse.optString("message", "Proverite svoj email za novu lozinku.")
                    runOnUiThread {
                        Toast.makeText(this@ForgotPasswordActivity, message, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    val errorResponse = responseBody?.let { JSONObject(it).optString("error", "Greška pri obradi zahteva.") }
                    println("Greška servera: $errorResponse") // Dodato za debug
                    runOnUiThread {
                        Toast.makeText(this@ForgotPasswordActivity, errorResponse, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
