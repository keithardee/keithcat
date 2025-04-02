package com.example.purrfectpaircat.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// Request Models
data class RegisterRequest(
    val fullname: String,
    val email: String,
    val contactNumber: String,
    val facebookName: String,
    val homeAddress: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

// Response Models
data class RegisterResponse(
    val error: Boolean,
    val message: String
)

data class LoginResponse(
    val error: Boolean,
    val message: String,
    val token: String,
    val user: User?
)

// User Model
data class User(
    val id: Int,
    val fullname: String,
    val email: String,
    val contactNumber: String,
    val facebookName: String,
    val homeAddress: String
)

// API Interface
interface AuthService {
    @POST("mobile/register.php")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("mobile/login.php")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}
