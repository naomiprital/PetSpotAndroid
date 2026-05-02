package com.example.petspotandroid.features.posts_list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.petspotandroid.R
import com.example.petspotandroid.dao.AppLocalDb
import com.example.petspotandroid.model.Post
import com.example.petspotandroid.data.repository.fact.FactRepository
import com.example.petspotandroid.data.repository.post.PostRepository
import kotlinx.coroutines.launch

enum class FilterType { ALL, LOST, FOUND }
enum class SortOrder { NEWEST_FIRST, OLDEST_FIRST }

class PostsViewModel(application: Application) : AndroidViewModel(application) {
//    TODO: Break down to smaller view models !!
    private val postDao = AppLocalDb.getDatabase(application).postDao()
    private val repository = PostRepository(postDao)

    private val _filteredPosts = MediatorLiveData<List<Post>>()
    val filteredPosts: LiveData<List<Post>> get() = _filteredPosts

    private var currentType = FilterType.ALL
    private var currentAnimal: String? = null
    private var currentSort = SortOrder.NEWEST_FIRST
    private var currentSearchQuery = ""

    private val factRepository = FactRepository()
    private val _dailyFact = MutableLiveData<String?>()
    val dailyFact: LiveData<String?> = _dailyFact

    init {
        refreshPosts()

        _filteredPosts.addSource(repository.allPosts) { posts ->
            applyFilters(posts)
        }
    }

    fun refreshPosts() {
        viewModelScope.launch {
            repository.refreshPosts()
        }
    }

    fun addPost(post: Post, onResult: (Boolean, Int) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val result = repository.addPost(post)
            if (result.isSuccess) {
                onResult(true, R.string.report_published)
            } else {
                onResult(false, R.string.failed_to_publish)
            }
        }
    }

    fun updatePost(post: Post, onResult: (Boolean, Int) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val result = repository.updatePost(post)
            if (result.isSuccess) {
                onResult(true, R.string.report_updated)
            } else {
                onResult(false, R.string.failed_to_update)
            }
        }
    }

    fun deletePost(post: Post, onResult: (Boolean, Int) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val result = repository.deletePost(post)
            if (result.isSuccess) {
                onResult(true, R.string.report_deleted)
            } else {
                onResult(false, R.string.failed_to_delete)
            }
        }
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



    fun loadDailyFact(supportedAnimals: List<String>) {
        viewModelScope.launch {
            val fact = factRepository.getDailyFact(supportedAnimals)
            _dailyFact.postValue(fact)
        }
    }
}