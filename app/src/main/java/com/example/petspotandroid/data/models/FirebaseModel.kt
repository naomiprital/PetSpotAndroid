package com.example.petspotandroid.data.models

import android.util.Log
import com.example.petspotandroid.base.FirestoreCompletion
import com.example.petspotandroid.base.FirestorePostsCompletion
import com.example.petspotandroid.base.FirestoreUserCompletion
import com.example.petspotandroid.model.Post
import com.example.petspotandroid.model.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class FirebaseModel {
    private val db = FirebaseFirestore.getInstance()

    private companion object COLLECTIONS {
        const val USERS = "users"
        const val POSTS = "posts"
    }

    fun createUser(user: User, completion: FirestoreCompletion) {
        db.collection(USERS)
            .document(user.id)
            .set(user.toJson)
            .addOnSuccessListener {
                completion(true, null)
            }
            .addOnFailureListener { exception ->
                completion(false, exception.message)
            }
    }

    fun getUser(userId: String, completion: FirestoreUserCompletion) {
        db.collection(USERS)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists() && document.data != null) {
                    try {
                        val data = document.data!!.toMutableMap()
                        data[User.ID_KEY] = document.id
                        val user = User.fromJson(data)
                        completion(user, null)
                    } catch (e: Exception) {
                        completion(null, e.message)
                    }
                } else {
                    completion(null, "User not found")
                }
            }
            .addOnFailureListener { exception ->
                completion(null, exception.message)
            }
    }

    fun updateUser(user: User, completion: FirestoreCompletion) {
        db.collection(USERS)
            .document(user.id)
            .update(user.toJson)
            .addOnSuccessListener {
                completion(true, null)
            }
            .addOnFailureListener { exception ->
                completion(false, exception.message)
            }
    }

    fun addPost(post: Post, callback: (Boolean, String?) -> Unit) {
        db.collection(POSTS)
            .document(post.id)
            .set(post.toJson)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message ?: "Unknown error occurred while adding to Firestore")
                }
            }
    }

    fun getAllPosts(since: Long, completion: FirestorePostsCompletion) {
        val timestamp = Timestamp(Date(since))

        db.collection(POSTS)
            .whereGreaterThanOrEqualTo(Post.LAST_UPDATED_KEY, timestamp)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    if (result != null) {
                        val posts = result.mapNotNull { document ->
                            try {
                                val data = document.data.toMutableMap()
                                data[Post.ID_KEY] = document.id
                                Post.fromJson(data)
                            } catch (exception: Exception) {
                                Log.e("FirebaseModel", "Error converting document ${document.id} to Post", exception)
                                null
                            }
                        }
                        completion(posts)
                    } else {
                        completion(emptyList())
                    }
                } else {
                    Log.e("FirebaseModel", "Failed to fetch posts from Firebase", task.exception)
                    completion(emptyList())
                }
            }
    }

    fun updatePost(post: Post, callback: (Boolean, String?) -> Unit) {
        db.collection(POSTS)
            .document(post.id)
            .set(post.toJson)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message ?: "Unknown error occurred while updating Firestore")
                }
            }
    }

    fun deletePost(postId: String, completion: FirestoreCompletion) {
        db.collection(POSTS)
            .document(postId)
            .delete()
            .addOnSuccessListener {
                completion(true, null)
            }
            .addOnFailureListener { exception ->
                completion(false, exception.message)
            }
    }

    fun getUserPostsCount(userId: String, onComplete: (Int, Int) -> Unit) {
        db.collection(POSTS)
            .whereEqualTo("authorId", userId)
            .get()
            .addOnSuccessListener { docs ->
                val total = docs.size()
                val reunions = docs.count { it.getBoolean("isResolved") == true }
                onComplete(total, reunions)
            }
            .addOnFailureListener {
                onComplete(0, 0)
            }
    }
}