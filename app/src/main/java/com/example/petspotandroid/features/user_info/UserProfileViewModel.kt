package com.example.petspotandroid.features.user_info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petspotandroid.data.repository.auth.AuthRepository
import com.example.petspotandroid.model.User

class UserProfileViewModel : ViewModel() {
    private val repository = AuthRepository.instance

    fun getUserData(userId: String): LiveData<User?> {
        return repository.getUserById(userId)
    }

    fun getUserStats(userId: String): LiveData<Pair<Int, Int>> {
        val statsLiveData = MutableLiveData<Pair<Int, Int>>()
        repository.getUserStats(userId) { result ->
            statsLiveData.value = result
        }
        return statsLiveData
    }
}