package com.example.musicroom.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String
)

data class RegisterResponse(
    val message: String
)

interface ApiService {
    @POST("/register")
    fun registerUser(@Body request: RegisterRequest): Call<RegisterResponse>
}
