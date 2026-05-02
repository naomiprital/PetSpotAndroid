package com.example.petspotandroid.features.post_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.petspotandroid.data.repository.post.PostRepository
import com.example.petspotandroid.model.Comment
import com.example.petspotandroid.model.Post

class PostDetailsViewModel : ViewModel() {

    private val repository = PostRepository.instance

    fun getPost(postId: String): LiveData<Post> {
        return repository.getPostById(postId)
    }

    fun addComment(post: Post, comment: Comment, onResult: (Boolean) -> Unit) {
        val updatedComments = post.comments.toMutableList().apply { add(comment) }
        val updatedPost = post.copy(comments = updatedComments)

        repository.updatePost(updatedPost, null,
            onSuccess = { onResult(true) },
            onError = { onResult(false) }
        )
    }
}