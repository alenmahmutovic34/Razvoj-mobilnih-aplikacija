package com.example.musicroom

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)

        val usernameEditText: EditText = findViewById(R.id.usernameEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val createAccountButton: Button = findViewById(R.id.createAccountButton)
        val backToHomeButton: Button = findViewById(R.id.backToHomeButton)

        createAccountButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Molimo popunite sva polja!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidEmail(email)) {
                Toast.makeText(this, "Molimo unesite validnu email adresu sa @ i .com", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Provera da li korisničko ime već postoji
            checkUsernameAvailability(username) { isAvailable ->
                if (isAvailable) {
                    registerUser(username, password, email)
                } else {
                    Toast.makeText(this, "Korisničko ime je već zauzeto!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        backToHomeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Funkcija koja proverava dostupnost korisničkog imena
    private fun checkUsernameAvailability(username: String, callback: (Boolean) -> Unit) {
        val url = "https://zavrsnirmas.onrender.com/check-username" // Novi endpoint za proveru korisničkog imena
        val client = OkHttpClient()

        val json = JSONObject().apply {
            put("username", username)
        }

        val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json.toString())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    //Toast.makeText(this@RegisterActivity, "Greška pri proveri korisničkog imena: ${e.message}", Toast.LENGTH_SHORT).show()
                    callback(true)  // Pretpostavimo da je korisničko ime dostupno ako je došlo do greške
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        val jsonResponse = JSONObject(response.body?.string() ?: "")
                        val isAvailable = jsonResponse.optBoolean("isAvailable", false)
                        callback(isAvailable)
                    } else {
                        //Toast.makeText(this@RegisterActivity, "Greška pri proveri korisničkog imena.", Toast.LENGTH_SHORT).show()
                        callback(true)
                    }
                }
            }
        })
    }

    // Funkcija koja proverava validnost emaila
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.com$".toRegex()
        return email.matches(emailRegex)
    }

    private fun registerUser(username: String, password: String, email: String) {
        val url = "https://zavrsnirmas.onrender.com/register" // URL za registraciju
        val client = OkHttpClient()

        val json = JSONObject().apply {
            put("username", username)
            put("password", password)
            put("email", email)
        }

        val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json.toString())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "Greška: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "Korisnik uspešno registrovan!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Registracija nije uspela!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
