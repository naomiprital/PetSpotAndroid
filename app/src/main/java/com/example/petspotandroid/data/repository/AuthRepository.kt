package com.example.petspotandroid.data.repository

import android.graphics.Bitmap
import com.example.petspotandroid.dao.UserDao
import com.example.petspotandroid.data.firebase.FirebaseStorageModel
import com.example.petspotandroid.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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

            withContext(Dispatchers.IO) {
                userDao.registerUser(userProfile)
            }

            Result.success(firebaseUser)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun updateUserProfile(
        firstName: String,
        lastName: String,
        phone: String,
        image: Bitmap? = null
    ): Result<User> {
        return try {
            val currentUser = auth.currentUser ?: throw Exception("User not logged in")
            val userId = currentUser.uid

            val document = firestore.collection("users").document(userId).get().await()
            val existingUser = document.toObject(User::class.java) ?: throw Exception("User not found")

            var updatedUser = existingUser.copy(
                firstName = firstName,
                lastName = lastName,
                phone = phone
            )

            if (image != null) {
                val imageUrl = uploadImage(image, updatedUser)
                if (imageUrl != null) {
                    updatedUser = updatedUser.copy(avatarUrl = imageUrl)
                }
            }

            firestore.collection("users").document(userId).set(updatedUser).await()

            withContext(Dispatchers.IO) {
                userDao.registerUser(updatedUser)
            }

            Result.success(updatedUser)
        } catch (exception: Exception) {
            Result.failure(exception)
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
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun getUserData(userId: String): Result<User> {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            val remoteUser = document.toObject(User::class.java)

            if (remoteUser != null) {
                withContext(Dispatchers.IO) {
                    userDao.registerUser(remoteUser)
                }
                Result.success(remoteUser)
            } else {
                val localUser = withContext(Dispatchers.IO) { userDao.getUserById(userId) }
                if (localUser != null) {
                    Result.success(localUser)
                } else {
                    Result.failure(Exception("User data not found in Firestore or Local DB"))
                }
            }
        } catch (exception: Exception) {
            val localUser = withContext(Dispatchers.IO) { userDao.getUserById(userId) }
            if (localUser != null) {
                Result.success(localUser)
            } else {
                Result.failure(exception)
            }
        }
    }

    @Suppress("DEPRECATION")
    suspend fun checkEmailExists(email: String): Result<Boolean> {
        return try {
            val result = auth.fetchSignInMethodsForEmail(email).await()
            val exists = !result.signInMethods.isNullOrEmpty()
            Result.success(exists)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun resetPassword(email: String): Result<Boolean> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(true)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}