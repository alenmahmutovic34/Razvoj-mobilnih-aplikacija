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

class ChangePasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val oldPasswordEditText = findViewById<EditText>(R.id.oldPasswordEditText)
        val newPasswordEditText = findViewById<EditText>(R.id.newPasswordEditText)
        val changePasswordButton = findViewById<Button>(R.id.changePasswordButton)

        changePasswordButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val oldPassword = oldPasswordEditText.text.toString().trim()
            val newPassword = newPasswordEditText.text.toString().trim()

            if (username.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "Molimo popunite sva polja!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val url = "http://10.0.2.2:8080/changePassword" // Adresa servera (localhost za emulator)
            val client = OkHttpClient()

            val json = JSONObject().apply {
                put("username", username)
                put("oldPassword", oldPassword)
                put("newPassword", newPassword)
            }

            val body = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            // Poziv servera za promenu lozinke
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@ChangePasswordActivity, "Greška: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(this@ChangePasswordActivity, "Lozinka uspešno promenjena!", Toast.LENGTH_SHORT).show()
                            finish() // Zatvaranje aktivnosti nakon uspeha
                        } else {
                            Toast.makeText(this@ChangePasswordActivity, "Greška: ${response.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }
    }
}
