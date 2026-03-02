package com.example.petspotandroid.models

import androidx.lifecycle.LiveData

class PostRepository(private val dao: PostDao) {
    val allPosts: LiveData<List<Post>> = dao.getAllPosts()
    suspend fun add(post: Post) {
        dao.insert(post)
    }

    suspend fun delete(post: Post) {
        dao.delete(post)
    }
}