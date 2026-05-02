package com.example.petspotandroid.data.repository.post

import androidx.lifecycle.LiveData
import com.example.petspotandroid.dao.PostDao
import com.example.petspotandroid.model.Post
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PostRepository(private val postDao: PostDao) {

    private val db = FirebaseFirestore.getInstance()
    private val postsCollection = db.collection("posts")

    val allPosts: LiveData<List<Post>> = postDao.getAllPosts()

    init {
        listenForPosts()
    }
    private fun listenForPosts() {
        postsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener

            if (snapshot != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    for (dc in snapshot.documentChanges) {
                        val post = dc.document.toObject(Post::class.java)

                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                postDao.insertPosts(listOf(post))
                            }
                            DocumentChange.Type.MODIFIED -> {
                                postDao.insertPosts(listOf(post))
                            }
                            DocumentChange.Type.REMOVED -> {
                                postDao.delete(post)
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun refreshPosts(): Result<Boolean> {
        return try {
            val snapshot = postsCollection.get().await()
            val remotePosts = snapshot.toObjects(Post::class.java)

            withContext(Dispatchers.IO) {
                postDao.deleteAll()
                postDao.insertPosts(remotePosts)
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addPost(post: Post): Result<Boolean> {
        return try {
            postsCollection.document(post.id).set(post).await()

            withContext(Dispatchers.IO) {
                postDao.insertPosts(listOf(post))
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePost(post: Post): Result<Boolean> {
        return try {
            postsCollection.document(post.id).set(post).await()

            withContext(Dispatchers.IO) {
                postDao.insertPosts(listOf(post))
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePost(post: Post): Result<Boolean> {
        return try {
            postsCollection.document(post.id).delete().await()

            withContext(Dispatchers.IO) {
                postDao.delete(post)
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}