package com.example.petspotandroid.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.petspotandroid.dao.PostDao
import com.example.petspotandroid.data.models.Post
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PostRepository(private val postDao: PostDao) {

    private val db = FirebaseFirestore.getInstance()
    private val postsCollection = db.collection("posts")

    val allPosts: LiveData<List<Post>> = postDao.getAllPosts()

    init {
        listenForPosts()
    }

    private fun listenForPosts() {
        postsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("PostRepository", "Listen failed.", error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val remotePosts = snapshot.toObjects(Post::class.java)

                CoroutineScope(Dispatchers.IO).launch {
                    postDao.insertPosts(remotePosts)
                }
            }
        }
    }

    fun refreshPosts() {
        postsCollection.get().addOnSuccessListener { snapshot ->
            val remotePosts = snapshot.toObjects(Post::class.java)
            CoroutineScope(Dispatchers.IO).launch {
                postDao.insertPosts(remotePosts)
            }
        }.addOnFailureListener { e ->
            Log.e("PostRepository", "Failed to refresh posts", e)
        }
    }

    fun addPost(post: Post, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        postsCollection.document(post.id).set(post)
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    postDao.insertPosts(listOf(post))
                }
                onSuccess()
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun updatePost(post: Post, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        postsCollection.document(post.id).set(post)
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    postDao.insertPosts(listOf(post))
                }
                onSuccess()
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun deletePost(post: Post, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        postsCollection.document(post.id).delete()
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    postDao.delete(post)
                }
                onSuccess()
            }
            .addOnFailureListener { e -> onFailure(e) }
    }
}