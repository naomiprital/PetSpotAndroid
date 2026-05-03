package com.example.petspotandroid.data.repository.auth

import android.graphics.Bitmap
import android.os.Looper
import androidx.core.os.HandlerCompat
import androidx.lifecycle.LiveData
import com.example.petspotandroid.base.MyApplication
import com.example.petspotandroid.dao.AppLocalDB
import com.example.petspotandroid.data.models.FirebaseAuthModel
import com.example.petspotandroid.data.models.FirebaseModel
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

    private val authModel = FirebaseAuthModel()
    private val firestoreModel = FirebaseModel()
    private val storageModel = FirebaseStorageModel()

    private val executor = Executors.newFixedThreadPool(4)
    private val mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())

    fun getUserLiveData(userId: String): LiveData<User?> = userDao.getUserById(userId)

    fun refreshUserData(userId: String) {
        firestoreModel.getUser(userId) { remoteUser, error ->
            if (remoteUser != null) {
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
        authModel.signInUser(email, password) { success, error ->
            if (success) {
                val firebaseUser = authModel.getCurrentUser()
                if (firebaseUser != null) {
                    refreshUserData(firebaseUser.uid)
                    callback(Result.success(firebaseUser))
                } else {
                    callback(Result.failure(Exception("User session error")))
                }
            } else {
                callback(Result.failure(Exception(error ?: "Login failed")))
            }
        }
    }

    fun register(user: User, password: String, image: Bitmap?, callback: (Result<FirebaseUser>) -> Unit) {
        authModel.createUser(email = user.email, password = password) { success, error ->
            if (success) {
                val firebaseUser = authModel.getCurrentUser() ?: return@createUser
                var userProfile = user.copy(id = firebaseUser.uid, lastUpdated = System.currentTimeMillis())

                if (image != null) {
                    storageModel.uploadUserImage(image, userProfile) { url ->
                        if (url != null) userProfile = userProfile.copy(avatarUrl = url)
                        saveUserToFirestore(userProfile, firebaseUser, callback)
                    }
                } else {
                    saveUserToFirestore(userProfile, firebaseUser, callback)
                }
            } else {
                callback(Result.failure(Exception(error)))
            }
        }
    }

    private fun <T> saveUserToFirestore(user: User, resultData: T, callback: (Result<T>) -> Unit) {
        firestoreModel.createUser(user) { success, error ->
            if (success) {
                executor.execute {
                    userDao.insertUser(user)
                    user.lastUpdated?.let { User.lastUpdated = it }
                    mainHandler.post { callback(Result.success(resultData)) }
                }
            } else {
                mainHandler.post { callback(Result.failure(Exception(error))) }
            }
        }
    }

    fun logout() {
        authModel.logout()
    }

    fun getCurrentUser(): FirebaseUser? = authModel.getCurrentUser()

    fun checkEmailExists(email: String, callback: (Result<Boolean>) -> Unit) {
        authModel.checkEmailExists(email) { exists, error ->
            if (error == null) {
                callback(Result.success(exists))
            } else {
                callback(Result.failure(Exception(error)))
            }
        }
    }

    fun resetPassword(email: String, callback: (Result<Boolean>) -> Unit) {
        authModel.resetPassword(email) { success, error ->
            if (success) {
                callback(Result.success(true))
            } else {
                callback(Result.failure(Exception(error)))
            }
        }
    }

    fun updateUserProfile(
        firstName: String,
        lastName: String,
        phone: String,
        image: Bitmap?,
        callback: (Result<User>) -> Unit
    ) {
        val firebaseUser = authModel.getCurrentUser()
        if (firebaseUser == null) {
            callback(Result.failure(Exception("User not logged in")))
            return
        }

        firestoreModel.getUser(firebaseUser.uid) { existingUser, error ->
            if (existingUser == null) {
                callback(Result.failure(Exception(error ?: "User not found")))
                return@getUser
            }

            var updatedUser = existingUser.copy(
                firstName = firstName,
                lastName = lastName,
                phone = phone
            )

            if (image != null) {
                storageModel.uploadUserImage(image, updatedUser) { url ->
                    if (url != null) updatedUser = updatedUser.copy(avatarUrl = url)
                    saveUpdatedUser(updatedUser, callback)
                }
            } else {
                saveUpdatedUser(updatedUser, callback)
            }
        }
    }

    private fun saveUpdatedUser(user: User, callback: (Result<User>) -> Unit) {
        firestoreModel.updateUser(user) { success, error ->
            if (success) {
                executor.execute {
                    userDao.insertUser(user)
                    mainHandler.post { callback(Result.success(user)) }
                }
            } else {
                mainHandler.post { callback(Result.failure(Exception(error))) }
            }
        }
    }
}