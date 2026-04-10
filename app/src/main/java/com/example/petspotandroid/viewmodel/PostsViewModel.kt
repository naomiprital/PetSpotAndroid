package com.example.petspotandroid.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.map
import com.example.petspotandroid.dao.AppLocalDb
import com.example.petspotandroid.data.models.Post
import com.example.petspotandroid.data.repository.PostRepository

enum class FilterType { ALL, LOST, FOUND }
enum class SortOrder { NEWEST_FIRST, OLDEST_FIRST }

class PostsViewModel(application: Application) : AndroidViewModel(application) {

    private val postDao = AppLocalDb.getDatabase(application).postDao()
    private val repository = PostRepository(postDao)

    private val _filteredPosts = MediatorLiveData<List<Post>>()
    val filteredPosts: LiveData<List<Post>> get() = _filteredPosts

    private var currentType = FilterType.ALL
    private var currentAnimal: String? = null
    private var currentSort = SortOrder.NEWEST_FIRST
    private var currentSearchQuery = ""

    init {
        refreshPosts()

        _filteredPosts.addSource(repository.allPosts) { posts ->
            applyFilters(posts)
        }
    }


    fun refreshPosts() {
        repository.refreshPosts()
    }


    fun addPost(post: Post) {
        repository.addPost(
            post,
            onSuccess = { Log.d("PostsViewModel", "Post saved to Firebase & Room!") },
            onFailure = { e -> Log.e("PostsViewModel", "Failed to save post", e) }
        )
    }

    fun updatePost(post: Post) {
        repository.updatePost(
            post,
            onSuccess = { Log.d("PostsViewModel", "Post updated!") },
            onFailure = { e -> Log.e("PostsViewModel", "Failed to update post", e) }
        )
    }

    fun deletePost(post: Post) {
        repository.deletePost(
            post,
            onSuccess = { Log.d("PostsViewModel", "Post deleted!") },
            onFailure = { e -> Log.e("PostsViewModel", "Failed to delete post", e) }
        )
    }


    fun getMyPosts(userId: String): LiveData<List<Post>> {
        return repository.allPosts.map { posts ->
            posts.filter { it.authorId == userId }
        }
    }


    fun updateFilters(type: FilterType, animal: String?, sort: SortOrder) {
        currentType = type
        currentAnimal = animal
        currentSort = sort
        applyFilters(repository.allPosts.value)
    }

    fun updateSearchQuery(query: String) {
        currentSearchQuery = query
        applyFilters(repository.allPosts.value)
    }

    private fun applyFilters(posts: List<Post>?) {
        var result = posts ?: emptyList()

        if (currentSearchQuery.isNotBlank()) {
            result = result.filter {
                it.description.contains(currentSearchQuery, ignoreCase = true) ||
                        it.lastSeenLocation.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        if (currentType != FilterType.ALL) {
            val lookingForLost = currentType == FilterType.LOST
            result = result.filter { it.isLost == lookingForLost }
        }

        if (currentAnimal != null) {
            result = result.filter { it.petType.equals(currentAnimal, ignoreCase = true) }
        }

        result = if (currentSort == SortOrder.OLDEST_FIRST) {
            result.sortedBy { it.createdAt }
        } else {
            result.sortedByDescending { it.createdAt }
        }

        _filteredPosts.value = result
    }
}