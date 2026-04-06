package com.example.petspotandroid.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.petspotandroid.dao.UserDao
import com.example.petspotandroid.data.firebase.FirebaseStorageModel
import com.example.petspotandroid.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

class AuthRepository(private val userDao: UserDao) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseStorageModel = FirebaseStorageModel()

    suspend fun register(
        user: User,
        password: String,
        image: Bitmap? = null
    ): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(user.email, password).await()
            val firebaseUser =
                authResult.user ?: throw Exception("User creation failed: ID is null")
            val userId = firebaseUser.uid
            
            var userProfile = user.copy(id = userId)

            if (image != null) {
                val imageUrl = uploadImage(image, userProfile)
                if (imageUrl != null) {
                    userProfile = userProfile.copy(avatarUrl = imageUrl)
                }
            }

            firestore.collection("users").document(userId).set(userProfile).await()
            userDao.registerUser(userProfile)

            Result.success(firebaseUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun uploadImage(image: Bitmap, user: User): String? = suspendCancellableCoroutine { continuation ->
        firebaseStorageModel.uploadUserImage(image, user) { url ->
            continuation.resume(url)
        }
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("Login failed: User is null")

            getUserData(firebaseUser.uid)

            Result.success(firebaseUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserData(userId: String): Result<User> {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            val remoteUser = document.toObject(User::class.java)
            
            if (remoteUser != null) {
                userDao.registerUser(remoteUser)
                Result.success(remoteUser)
            } else {
                val localUser = userDao.getUserById(userId)
                if (localUser != null) {
                    Result.success(localUser)
                } else {
                    Result.failure(Exception("User data not found in Firestore or Local DB"))
                }
            }
        } catch (e: Exception) {
            val localUser = userDao.getUserById(userId)
            if (localUser != null) {
                Result.success(localUser)
            } else {
                Result.failure(e)
            }
        }
    }

//    TODO: Implement
    suspend fun resetPassword(email: String): Result<Boolean> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}
