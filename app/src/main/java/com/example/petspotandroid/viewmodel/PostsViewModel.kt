package com.example.petspotandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petspotandroid.models.Post
import com.example.petspotandroid.repository.PostRepository

enum class FilterType {
    ALL,
    LOST,
    FOUND
}

enum class SortOrder {
    NEWEST_FIRST,
    OLDEST_FIRST
}

class PostsViewModel : ViewModel() {

    private val repository = PostRepository()

    private val _allPosts = MutableLiveData<List<Post>>(emptyList())
    private val _filteredPosts = MutableLiveData<List<Post>>(emptyList())
    val filteredPosts: LiveData<List<Post>> get() = _filteredPosts

    private var currentType = FilterType.ALL
    private var currentAnimal: String? = null
    private var currentSort = SortOrder.NEWEST_FIRST
    private var currentSearchQuery = ""

    init {
        _allPosts.value = repository.getAllPosts()
        applyFilters()
    }

    fun addPost(post: Post) {
        repository.addPost(post)

        _allPosts.value = repository.getAllPosts()
        applyFilters()
    }

    fun updateFilters(type: FilterType, animal: String?, sort: SortOrder) {
        currentType = type
        currentAnimal = animal
        currentSort = sort
        applyFilters()
    }

    fun updateSearchQuery(query: String) {
        currentSearchQuery = query
        applyFilters()
    }

    private fun applyFilters() {
        var result = _allPosts.value ?: emptyList()

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