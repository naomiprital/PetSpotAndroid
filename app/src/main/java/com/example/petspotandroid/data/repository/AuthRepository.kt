package com.example.petspotandroid.data.repository

import com.example.petspotandroid.dao.UserDao
import com.example.petspotandroid.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(private val userDao: UserDao) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun register(
        user: User,
        password: String,
    ): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(user.email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("User creation failed: ID is null")
            val userId = firebaseUser.uid
            val userProfile = user.copy(id = userId)

            // Save to Firestore
            firestore.collection("users").document(userId).set(userProfile).await()
            
            // Save to Local DB
            userDao.registerUser(userProfile)
            
            Result.success(firebaseUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("Login failed: User is null")
            
            var localUser = userDao.getUserById(firebaseUser.uid)
            if (localUser == null) {
                val document = firestore.collection("users").document(firebaseUser.uid).get().await()
                localUser = document.toObject(User::class.java)
                if (localUser != null) {
                    userDao.registerUser(localUser)
                }
            }
            Result.success(firebaseUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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