package com.example.petspotandroid.features.posts_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petspotandroid.data.repository.fact.FactRepository
import com.example.petspotandroid.data.repository.post.PostRepository
import com.example.petspotandroid.model.Post

enum class FilterType { ALL, LOST, FOUND }
enum class SortOrder { NEWEST_FIRST, OLDEST_FIRST }

class PostsViewModel : ViewModel() {

    private val repository = PostRepository.instance
    private val factRepository = FactRepository.instance

    private val filterTrigger = MutableLiveData<Unit>()

    private var lastKnownPosts: List<Post> = emptyList()

    private val _filteredPosts = MediatorLiveData<List<Post>>()
    val filteredPosts: LiveData<List<Post>> get() = _filteredPosts

    private var currentType = FilterType.ALL
    private var currentAnimal: String? = null
    private var currentSort = SortOrder.NEWEST_FIRST
    private var currentSearchQuery = ""

    private val _dailyFact = MutableLiveData<String?>()
    val dailyFact: LiveData<String?> = _dailyFact

    init {
        refreshPosts()

        _filteredPosts.addSource(repository.getAllPosts()) { posts ->
            lastKnownPosts = posts ?: emptyList()
            _filteredPosts.value = applyFilters(lastKnownPosts)
        }

        _filteredPosts.addSource(filterTrigger) {
            _filteredPosts.value = applyFilters(lastKnownPosts)
        }
    }

    fun refreshPosts() {
        repository.refreshPosts()
    }

    fun updateFilters(type: FilterType, animal: String?, sort: SortOrder) {
        currentType = type
        currentAnimal = animal
        currentSort = sort
        filterTrigger.value = Unit
    }

    fun updateSearchQuery(query: String) {
        currentSearchQuery = query
        filterTrigger.value = Unit
    }

    private fun applyFilters(posts: List<Post>): List<Post> {
        return posts.filter { post ->
            val matchesSearch = currentSearchQuery.isBlank() ||
                    post.description.contains(currentSearchQuery, ignoreCase = true) ||
                    post.lastSeenLocation.contains(currentSearchQuery, ignoreCase = true)

            val matchesType = currentType == FilterType.ALL ||
                    post.isLost == (currentType == FilterType.LOST)

            val matchesAnimal = currentAnimal == null ||
                    post.petType.trim().equals(currentAnimal?.trim(), ignoreCase = true)

            matchesSearch && matchesType && matchesAnimal
        }.let { filtered ->
            if (currentSort == SortOrder.OLDEST_FIRST) {
                filtered.sortedBy { it.createdAt }
            } else {
                filtered.sortedByDescending { it.createdAt }
            }
        }
    }

    fun loadDailyFact(supportedAnimals: List<String>) {
        factRepository.getDailyFact(supportedAnimals) { fact ->
            _dailyFact.value = fact
        }
    }

    fun updatePost(post: Post, imageBytes: ByteArray? = null, onResult: (Boolean) -> Unit) {
        repository.updatePost(
            post = post,
            imageBytes = imageBytes,
            onSuccess = {
                onResult(true)
            },
            onError = { errorMessage ->
                onResult(false)
            }
        )
    }

    fun deletePost(post: Post, onResult: (Boolean, String?) -> Unit) {
        repository.deletePost(post,
            onSuccess = { onResult(true, null) },
            onError = { error -> onResult(false, error) }
        )
    }

    fun getMyPosts(userId: String): LiveData<List<Post>> {
        return repository.getPostByAuthorId(userId)
    }
}