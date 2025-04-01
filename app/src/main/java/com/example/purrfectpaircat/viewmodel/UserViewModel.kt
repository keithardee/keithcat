package com.example.purrfectpaircat.api.com.example.purrfectpaircat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    val userData = MutableLiveData<User>()

    fun setUserData(user: User) {
        userData.value = user
    }
}

data class User(
    val fullname: String,
    val email: String,
    val contactNumber: String,
    val facebookName: String,
    val homeAddress: String
)
