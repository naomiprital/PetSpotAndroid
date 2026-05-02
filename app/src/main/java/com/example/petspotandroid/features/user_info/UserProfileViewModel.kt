package com.example.petspotandroid.features.user_info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petspotandroid.model.User
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    fun getUserData(userId: String): LiveData<User?> {
        val userLiveData = MutableLiveData<User?>()
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                val data = doc.data
                userLiveData.value = if (data != null) User.fromJson(data) else null
            }
        return userLiveData
    }

    fun getUserStats(userId: String): LiveData<Pair<Int, Int>> {
        val statsLiveData = MutableLiveData<Pair<Int, Int>>()
        db.collection("posts")
            .whereEqualTo("authorId", userId)
            .get()
            .addOnSuccessListener { docs ->
                val total = docs.size()
                val reunions = docs.count { it.getBoolean("isResolved") == true }
                statsLiveData.value = Pair(total, reunions)
            }
        return statsLiveData
    }
}