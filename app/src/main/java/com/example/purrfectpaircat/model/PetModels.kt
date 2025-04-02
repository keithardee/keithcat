package com.example.purrfectpaircat.model

data class PetRequest(
    val user_id: Int,
    val name: String,
    val breed: String,
    val gender: String,
    val age: Int,
    val adopt_status: String,
    val vaccination: Int,
    val adddate: String,
    val imageUri: String? = null
)

data class PetResponse(
    val success: Boolean,
    val message: String
)
