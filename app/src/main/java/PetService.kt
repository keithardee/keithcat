package com.example.purrfectpaircat.api

import com.example.purrfectpaircat.model.PetRequest
import com.example.purrfectpaircat.model.PetResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// Request Model for Adding Pet
data class PetRequestBody(
    val user_id: Int,  // Make sure user_id is part of the request body
    val name: String,
    val breed: String,
    val gender: String,
    val age: String,
    val adopt_status: String,
    val vaccination: String,
    val adddate: String,
    val imageUri: String
)


// Response Model for Pet API
data class PetResponse(
    val error: Boolean,
    val message: String
)

// API Interface for Pet Service
interface PetService {
    @POST("mobile/addCat.php")
    fun addPet(@Body petRequest: PetRequest): Call<PetResponse>
}

