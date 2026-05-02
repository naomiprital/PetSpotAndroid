package com.example.petspotandroid.data.repository.auth

import android.graphics.Bitmap
import android.os.Looper
import androidx.core.os.HandlerCompat
import androidx.lifecycle.LiveData
import com.example.petspotandroid.base.MyApplication
import com.example.petspotandroid.dao.AppLocalDB
import com.example.petspotandroid.data.models.FirebaseStorageModel
import com.example.petspotandroid.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.Executors

class AuthRepository private constructor() {

    companion object {
        val instance = AuthRepository()
    }

    private val userDao = AppLocalDB.db.userDao
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val firebaseStorageModel = FirebaseStorageModel()
    private val executor = Executors.newFixedThreadPool(4)
    private val mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())

    fun getUserLiveData(userId: String): LiveData<User?> = userDao.getUserById(userId)

    fun refreshUserData(userId: String) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                document.data?.let { data ->
                    val remoteUser = User.fromJson(data)
                    executor.execute {
                        userDao.insertUser(remoteUser)
                        remoteUser.lastUpdated?.let {
                            if (it > User.lastUpdated) User.lastUpdated = it
                        }
                    }
                }
            }
    }

    fun login(email: String, password: String, callback: (Result<FirebaseUser>) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                result.user?.let {
                    refreshUserData(it.uid)
                    callback(Result.success(it))
                }
            }.addOnFailureListener { callback(Result.failure(it)) }
    }

    fun register(user: User, password: String, image: Bitmap?, callback: (Result<FirebaseUser>) -> Unit) {
        auth.createUserWithEmailAndPassword(user.email, password)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user ?: return@addOnSuccessListener

                var userProfile = user.copy(id = firebaseUser.uid, lastUpdated = System.currentTimeMillis())

                if (image != null) {
                    firebaseStorageModel.uploadUserImage(image, userProfile) { url ->
                        if (url != null) userProfile = userProfile.copy(avatarUrl = url)
                        saveUserToDatabase(userProfile, firebaseUser, callback)
                    }
                } else {
                    saveUserToDatabase(userProfile, firebaseUser, callback)
                }
            }.addOnFailureListener { callback(Result.failure(it)) }
    }

    private fun <T> saveUserToDatabase(user: User, resultData: T, callback: (Result<T>) -> Unit) {
        firestore.collection("users").document(user.id).set(user.toJson)
            .addOnSuccessListener {
                executor.execute {
                    userDao.insertUser(user)
                    user.lastUpdated?.let { User.lastUpdated = it }
                    mainHandler.post { callback(Result.success(resultData)) }
                }
            }.addOnFailureListener { callback(Result.failure(it)) }
    }

    fun updateUserProfile(
        firstName: String,
        lastName: String,
        phone: String,
        image: Bitmap?,
        callback: (Result<User>) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(Result.failure(Exception("User not logged in")))
            return
        }
        val userId = currentUser.uid

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val data = document.data
                val existingUser = if (data != null) User.fromJson(data) else null

                if (existingUser == null) {
                    callback(Result.failure(Exception("User not found")))
                    return@addOnSuccessListener
                }

                var updatedUser = existingUser.copy(
                    firstName = firstName,
                    lastName = lastName,
                    phone = phone
                )

                if (image != null) {
                    firebaseStorageModel.uploadUserImage(image, updatedUser) { imageUrl ->
                        if (imageUrl != null) updatedUser = updatedUser.copy(avatarUrl = imageUrl)
                        saveUserToDatabase(updatedUser, updatedUser, callback)
                    }
                } else {
                    saveUserToDatabase(updatedUser, updatedUser, callback)
                }
            }
            .addOnFailureListener { exception ->
                callback(Result.failure(exception))
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun checkEmailExists(email: String, callback: (Result<Boolean>) -> Unit) {
        auth.fetchSignInMethodsForEmail(email)
            .addOnSuccessListener { result ->
                callback(Result.success(!result.signInMethods.isNullOrEmpty()))
            }
            .addOnFailureListener { exception -> callback(Result.failure(exception)) }
    }

    fun resetPassword(email: String, callback: (Result<Boolean>) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { callback(Result.success(true)) }
            .addOnFailureListener { exception -> callback(Result.failure(exception)) }
    }
}