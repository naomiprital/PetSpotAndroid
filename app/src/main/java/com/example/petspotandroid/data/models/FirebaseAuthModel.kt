package com.example.petspotandroid.data.models

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Looper
import androidx.core.os.HandlerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.petspotandroid.base.PetSpotApplication
import com.example.petspotandroid.dao.AppLocalDb
import com.example.petspotandroid.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.Executors
import androidx.core.content.edit

class FirebaseAuthModel private constructor() {

    private val database: AppLocalDb = AppLocalDb.getDatabase(PetSpotApplication.context)
    private val executor = Executors.newFixedThreadPool(4)
    private val mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseStorageModel = FirebaseStorageModel()

    companion object {
        val instance = FirebaseAuthModel()
    }

    private val _user = MutableLiveData<FirebaseUser?>()

    val user: LiveData<FirebaseUser?> get() = _user

    init {
        _user.value = auth.currentUser
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun getUserById(id: String): LiveData<User?> {
        return database.userDao().getUserById(id)
    }

    fun login(email: String, password: String, callback: (Result<FirebaseUser>) -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseUser = auth.currentUser
                _user.value = firebaseUser
                if (firebaseUser != null) {
                    refreshUserData(firebaseUser.uid)
                    callback(Result.success(firebaseUser))
                } else {
                    callback(Result.failure(Exception("User is null")))
                }
            } else {
                callback(Result.failure(task.exception ?: Exception("Login failed")))
            }
        }
    }

    fun registerWithUri(user: User, password: String, imageUri: Uri?, callback: (Result<FirebaseUser>) -> Unit) {
        auth.createUserWithEmailAndPassword(user.email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseUser = task.result?.user
                if (firebaseUser != null) {
                    val userId = firebaseUser.uid
                    var userProfile = user.copy(id = userId)
                    
                    if (imageUri != null) {
                        firebaseStorageModel.uploadUserImageFromUri(imageUri, userId) { url ->
                            if (url != null) userProfile = userProfile.copy(avatarUrl = url)
                            saveUserToFirestoreAndLocal(userProfile, firebaseUser, callback)
                        }
                    } else {
                        saveUserToFirestoreAndLocal(userProfile, firebaseUser, callback)
                    }
                }
            } else {
                callback(Result.failure(task.exception ?: Exception("Registration failed")))
            }
        }
    }

    private fun saveUserToFirestoreAndLocal(user: User, firebaseUser: FirebaseUser, callback: (Result<FirebaseUser>) -> Unit) {
        val lastUpdated = System.currentTimeMillis()
        val userToSave = user.copy(lastUpdated = lastUpdated)
        
        firestore.collection(User.COLLECTION_NAME).document(user.id).set(userToSave.toJson())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _user.value = firebaseUser
                    executor.execute {
                        database.userDao().insert(userToSave)
                        mainHandler.post { callback(Result.success(firebaseUser)) }
                    }
                } else {
                    callback(Result.failure(task.exception ?: Exception("Save failed")))
                }
            }
    }

    fun updateUserProfileWithUri(firstName: String, lastName: String, phone: String, imageUri: Uri?, callback: (Result<User>) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection(User.COLLECTION_NAME).document(userId).get().addOnSuccessListener { doc ->
            val existingUser = doc.data?.let { User.fromJson(it) } ?: User(id = userId)
            var updatedUser = existingUser.copy(firstName = firstName, lastName = lastName, phone = phone)

            if (imageUri != null) {
                firebaseStorageModel.uploadUserImageFromUri(imageUri, userId) { url ->
                    if (url != null) updatedUser = updatedUser.copy(avatarUrl = url)
                    saveUpdatedUser(updatedUser, callback)
                }
            } else {
                saveUpdatedUser(updatedUser, callback)
            }
        }
    }

    private fun saveUpdatedUser(user: User, callback: (Result<User>) -> Unit) {
        val lastUpdated = System.currentTimeMillis()
        val userToSave = user.copy(lastUpdated = lastUpdated)
        firestore.collection(User.COLLECTION_NAME).document(user.id).set(userToSave.toJson()).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                executor.execute {
                    database.userDao().insert(userToSave)
                    mainHandler.post { callback(Result.success(userToSave)) }
                }
            } else {
                callback(Result.failure(task.exception ?: Exception("Update failed")))
            }
        }
    }

    fun refreshUserData(userId: String) {
        val sp = PetSpotApplication.context.getSharedPreferences("PetSpotSync", Context.MODE_PRIVATE)
        val lastUpdated = sp.getLong("user_${userId}_lastUpdated", 0L)

        firestore.collection(User.COLLECTION_NAME).document(userId).get().addOnSuccessListener { document ->
            val user = document.data?.let { User.fromJson(it) }
            if (user != null && (user.lastUpdated ?: 0L) >= lastUpdated) {
                executor.execute { database.userDao().insert(user)
                sp.edit { putLong("user_${userId}_lastUpdated", user.lastUpdated ?: 0L) }
                }
            }
        }
    }

    fun logout() {
        auth.signOut()
        _user.value = null
    }

    fun resetPassword(email: String, callback: (Result<Unit>) -> Unit) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) callback(Result.success(Unit))
            else callback(Result.failure(task.exception ?: Exception("Error")))
        }
    }

    @Suppress("DEPRECATION")
    fun checkEmailExists(email: String, callback: (Result<Boolean>) -> Unit) {
        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val exists = !task.result?.signInMethods.isNullOrEmpty()
                callback(Result.success(exists))
            } else {
                callback(Result.failure(task.exception ?: Exception("Check failed")))
            }
        }
    }
}
