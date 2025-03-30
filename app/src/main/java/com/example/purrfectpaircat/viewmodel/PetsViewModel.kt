package com.example.purrfectpaircat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.purrfectpaircat.model.UserData

class PetsViewModel : ViewModel() {
    private val _petList = MutableLiveData<ArrayList<UserData>>(ArrayList())
    val petList: LiveData<ArrayList<UserData>> get() = _petList

    fun setPetList(pets: ArrayList<UserData>) {
        _petList.postValue(pets)
    }

    fun addPet(pet: UserData) {
        val updatedList = _petList.value ?: ArrayList()
        updatedList.add(pet)
        _petList.postValue(updatedList)

    }
}