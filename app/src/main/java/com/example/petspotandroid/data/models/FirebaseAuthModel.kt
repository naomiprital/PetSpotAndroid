package com.example.petspotandroid.data.models

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

typealias FirebaseAuthCompletion = (success: Boolean, error: String?) -> Unit

class FirebaseAuthModel {

    private var auth: FirebaseAuth = Firebase.auth

    fun logout() {
        auth.signOut()
    }

    fun createUser(email: String, password: String, completion: FirebaseAuthCompletion) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                completion(true, null)
            }
            .addOnFailureListener { exception ->
                completion(false, exception.message)
            }
    }

    fun signInUser(email: String, password: String, completion: FirebaseAuthCompletion) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                completion(true, null)
            }
            .addOnFailureListener { exception ->
                completion(false, exception.message)
            }
    }

    fun getCurrentUser() = auth.currentUser
}