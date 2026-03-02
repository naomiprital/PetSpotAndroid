package com.example.petspotandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petspotandroid.models.Post
import java.util.UUID

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
    private val _allPosts = MutableLiveData<List<Post>>(emptyList())
    private val _filteredPosts = MutableLiveData<List<Post>>(emptyList())
    val filteredPosts: LiveData<List<Post>> get() = _filteredPosts

    private var currentType = FilterType.ALL
    private var currentAnimal: String? = null
    private var currentSort = SortOrder.NEWEST_FIRST
    private var currentSearchQuery = ""

    init {
        loadMockData()
    }

    private fun loadMockData() {
        val now = System.currentTimeMillis()

        val post1 = Post(
            id = UUID.randomUUID().toString(),
            ownerId = "user1",
            userName = "Dana Cohen",
            text = "Lost my golden retriever near the park!",
            isLost = true,
            petType = "Dog",
            lastSeenLocation = "Hayarkon Park, Tel Aviv",
            imageUrl = "https://images.dog.ceo/breeds/retriever-golden/n02099601_100.jpg",
            timestamp = now - 3600000 // 1 hour ago (Added back for sorting!)
        )
        val post2 = Post(
            id = UUID.randomUUID().toString(),
            ownerId = "user2",
            userName = "Yossi Levi",
            text = "Found this sweet orange tabby wandering around.",
            isLost = false,
            petType = "Cat",
            lastSeenLocation = "Dizengoff Center",
            imageUrl = "https://cdn2.thecatapi.com/images/0XYvRd7oD.jpg",
            timestamp = now - 7200000
        )
        val post3 = Post(
            id = UUID.randomUUID().toString(),
            ownerId = "user3",
            userName = "Maya Golan",
            text = "My parrot flew out the window. Answers to 'Paco'.",
            isLost = true,
            petType = "Bird",
            lastSeenLocation = "Ramat Gan",
            imageUrl = "https://www.birdland.co.uk/wp-content/uploads/2013/03/Blue-Gold-Macaw-2.jpg",
            timestamp = now - 86400000
        )
        val post4 = Post(
            id = UUID.randomUUID().toString(),
            ownerId = "user4",
            userName = "Avraham",
            text = "Found a small poodle without a collar.",
            isLost = false,
            petType = "Dog",
            lastSeenLocation = "Jerusalem",
            imageUrl = "https://images.dog.ceo/breeds/poodle-toy/n02113624_9550.jpg",
            timestamp = now
        )

        _allPosts.value = listOf(post1, post2, post3, post4)
        applyFilters()
    }

    fun addPost(post: Post) {
        val currentList = _allPosts.value ?: emptyList()
        _allPosts.value = currentList + post
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
                it.text.contains(currentSearchQuery, ignoreCase = true) ||
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
            result.sortedBy { it.timestamp }
        } else {
            result.sortedByDescending { it.timestamp }
        }

        _filteredPosts.value = result
    }
}