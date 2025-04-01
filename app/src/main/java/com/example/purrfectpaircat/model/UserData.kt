package com.example.purrfectpaircat.model

class UserData(
    val name: String,
    val breed: String,
    val gender: String,
    val age: String,
    val adopt: String,
    val vaccination: String,
    val adddate: String,
    val imageUri: String?,

    val fullname: String? = null,
    val email: String? = null,
    val contactNumber: String? = null,
    val facebookName: String? = null,
    val address: String? = null
)