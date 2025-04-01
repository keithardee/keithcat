package com.example.purrfectpaircat.api.com.example.purrfectpaircat.model

data class LoginResponse(
    val error: Boolean,
    val message: String,
    val user: User?
)

data class User(
    val fullname: String,
    val email: String,
    val contactNumber: String,
    val facebookName: String,
    val homeAddress: String
)