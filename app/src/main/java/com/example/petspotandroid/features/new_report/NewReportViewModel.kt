package com.example.petspotandroid.features.new_report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petspotandroid.data.repository.post.PostRepository
import com.example.petspotandroid.model.Post

class NewReportViewModel : ViewModel() {

    private val repository = PostRepository.instance

    fun getPost(postId: String): LiveData<Post> {
        return repository.getPostById(postId)
    }

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading
    fun addPost(
        post: Post,
        imageBytes: ByteArray?,
        onResult: (success: Boolean) -> Unit
    ) {
        _isLoading.value = true
        repository.addPost(post, imageBytes,
            onSuccess = {
                _isLoading.postValue(false)
                onResult(true)
            },
            onError = {
                _isLoading.postValue(false)
                onResult(false)
            }
        )
    }

    fun updatePost(
        post: Post,
        imageBytes: ByteArray?,
        onResult: (success: Boolean) -> Unit
    ) {
        _isLoading.value = true
        repository.updatePost(post, imageBytes,
            onSuccess = {
                _isLoading.postValue(false)
                onResult(true)
            },
            onError = {
                _isLoading.postValue(false)
                onResult(false)
            }
        )
    }
}