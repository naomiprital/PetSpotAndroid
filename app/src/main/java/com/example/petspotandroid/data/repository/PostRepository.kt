package com.example.petspotandroid.data.repository

import com.example.petspotandroid.data.models.Post
import java.util.UUID

class PostRepository {

    private var mockPosts: List<Post> = emptyList()

    init {
        loadMockData()
    }

    fun getAllPosts(): List<Post> {
        return mockPosts
    }

    fun addPost(post: Post) {
        mockPosts = mockPosts + post
    }

    private fun loadMockData() {
        val now = System.currentTimeMillis()

        val post1 = Post(
            id = UUID.randomUUID().toString(),
            ownerId = "user1",
            userName = "Dana Cohen",
            description = "Lost my golden retriever near the park!",
            isLost = true,
            petType = "Dog",
            lastSeenLocation = "Hayarkon Park, Tel Aviv",
            imageUrl = "https://images.dog.ceo/breeds/retriever-golden/n02099601_100.jpg",
            timestamp = now - 3600000
        )
        val post2 = Post(
            id = UUID.randomUUID().toString(),
            ownerId = "user2",
            userName = "Yossi Levi",
            description = "Found this sweet orange tabby wandering around.",
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
            description = "My parrot flew out the window. Answers to 'Paco'.",
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
            description = "Found a small poodle without a collar.",
            isLost = false,
            petType = "Dog",
            lastSeenLocation = "Jerusalem",
            imageUrl = "https://images.dog.ceo/breeds/poodle-toy/n02113624_9550.jpg",
            timestamp = now
        )

        mockPosts = listOf(post1, post2, post3, post4)
    }
}