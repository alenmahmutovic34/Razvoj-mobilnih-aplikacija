package com.example.musicroom

import android.app.ProgressDialog
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

class ChangePasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val oldPasswordEditText = findViewById<EditText>(R.id.oldPasswordEditText)
        val newPasswordEditText = findViewById<EditText>(R.id.newPasswordEditText)
        val changePasswordButton = findViewById<Button>(R.id.changePasswordButton)
        val backToLoginButton = findViewById<Button>(R.id.backToLoginButton) // Dugme za povratak na login

        // Dugme za promenu lozinke
        changePasswordButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val oldPassword = oldPasswordEditText.text.toString().trim()
            val newPassword = newPasswordEditText.text.toString().trim()

            if (username.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "Molimo popunite sva polja!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Prikaz progres dijaloga
            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Menjam lozinku...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            val url = "https://zavrsnirmas.onrender.com/changePassword" // Adresa servera
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
                        progressDialog.dismiss()
                        Toast.makeText(this@ChangePasswordActivity, "Greška u konekciji: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            Toast.makeText(this@ChangePasswordActivity, "Lozinka uspešno promenjena!", Toast.LENGTH_SHORT).show()

                            // Resetovanje polja
                            oldPasswordEditText.text.clear()
                            newPasswordEditText.text.clear()

                            // Vraćanje na login ekran
                            val intent = Intent(this@ChangePasswordActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            val errorBody = response.body?.string()
                            val errorMessage = if (!errorBody.isNullOrEmpty()) {
                                try {
                                    JSONObject(errorBody).optString("error", "Neuspela promena lozinke")
                                } catch (e: Exception) {
                                    "Neuspela promena lozinke"
                                }
                            } else {
                                "Neuspela promena lozinke"
                            }
                            Toast.makeText(this@ChangePasswordActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }

        // Dugme za povratak na login ekran
        backToLoginButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
