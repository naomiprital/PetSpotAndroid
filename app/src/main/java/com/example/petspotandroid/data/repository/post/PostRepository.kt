package com.example.petspotandroid.data.repository.post

import android.os.Looper
import androidx.core.os.HandlerCompat
import androidx.lifecycle.LiveData
import com.example.petspotandroid.dao.AppLocalDB
import com.example.petspotandroid.data.models.FirebaseModel
import com.example.petspotandroid.data.models.StorageModel
import com.example.petspotandroid.model.Post
import java.util.concurrent.Executors

class PostRepository private constructor() {

    companion object {
        val instance = PostRepository()
    }

    private val firebaseModel = FirebaseModel()
    private val storageModel = StorageModel()

    private val executor = Executors.newFixedThreadPool(4)
    private val mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())

    private val postDao = AppLocalDB.db.postDao

    fun getAllPosts(): LiveData<List<Post>> {
        return postDao.getAllPosts()
    }

    fun refreshPosts() {
        val lastUpdated = Post.lastUpdated

        firebaseModel.getAllPosts(lastUpdated) { posts ->
            executor.execute {
                var latestTimestamp = lastUpdated

                for (post in posts) {
                    postDao.insertPost(post)

                    post.lastUpdated?.let {
                        if (it > latestTimestamp) {
                            latestTimestamp = it
                        }
                    }
                }
                Post.lastUpdated = latestTimestamp
            }
        }
    }

    fun addPost(post: Post, imageBytes: ByteArray?, onSuccess: () -> Unit, onError: (String) -> Unit) {
        executor.execute {
            if (imageBytes != null) {
                storageModel.uploadPostImage(imageBytes, post.id) { imageUrl ->
                    if (imageUrl != null) {
                        saveToFirebase(post.copy(imageUrl = imageUrl), onSuccess, onError, true)
                    } else {
                        mainHandler.post { onError("Image upload failed") }
                    }
                }
            } else {
                saveToFirebase(post, onSuccess, onError, true)
            }
        }
    }

    fun updatePost(post: Post, imageBytes: ByteArray?, onSuccess: () -> Unit, onError: (String) -> Unit) {
        executor.execute {
            if (imageBytes != null) {
                storageModel.uploadPostImage(imageBytes, post.id) { imageUrl ->
                    if (imageUrl != null) {
                        saveToFirebase(post.copy(imageUrl = imageUrl), onSuccess, onError, false)
                    } else {
                        mainHandler.post { onError("Image update failed") }
                    }
                }
            } else {
                saveToFirebase(post, onSuccess, onError, false)
            }
        }
    }

    private fun saveToFirebase(post: Post, onSuccess: () -> Unit, onError: (String) -> Unit, isNew: Boolean) {
        val action = if (isNew) firebaseModel::addPost else firebaseModel::updatePost

        action(post) { success, error ->
            if (success) {
                executor.execute {
                    postDao.insertPost(post)
                    mainHandler.post { onSuccess() }
                }
            } else {
                mainHandler.post { onError(error ?: "Operation failed") }
            }
        }
    }

    fun deletePost(post: Post, onSuccess: () -> Unit, onError: (String) -> Unit) {
        firebaseModel.deletePost(post.id) { success, error ->
            if (success) {
                executor.execute {
                    postDao.delete(post)
                    mainHandler.post { onSuccess() }
                }
            } else {
                mainHandler.post { onError(error ?: "Delete failed") }
            }
        }
    }

    fun getPostById(postId: String): LiveData<Post> {
        return postDao.getPostById(postId)
    }

    fun getPostByAuthorId(authorId: String): LiveData<List<Post>> {
        return postDao.getPostsByUser(authorId)
    }
}