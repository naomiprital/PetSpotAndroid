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
            authorId = "user1",
            userName = "Dana Cohen",
            description = "Lost my golden retriever near the park!",
            isLost = true,
            petType = "Dog",
            lastSeenLocation = "Hayarkon Park, Tel Aviv",
            imageUrl = "https://images.dog.ceo/breeds/retriever-golden/n02099601_100.jpg",
            createdAt = now - 3600000,
            eventDate = "Apr 04, 2026 at 12:00 PM",
            contactNumber = "054-123-4567"
        )
        val post2 = Post(
            id = UUID.randomUUID().toString(),
            authorId = "user2",
            userName = "Yossi Levi",
            description = "Found this sweet orange tabby wandering around.",
            isLost = false,
            petType = "Cat",
            lastSeenLocation = "Dizengoff Center",
            imageUrl = "https://cdn2.thecatapi.com/images/0XYvRd7oD.jpg",
            createdAt = now - 7200000,
            eventDate = "Apr 04, 2026 at 09:30 AM",
            contactNumber = "052-987-6543"
        )
        val post3 = Post(
            id = UUID.randomUUID().toString(),
            authorId = "user3",
            userName = "Maya Golan",
            description = "My parrot flew out the window. Answers to 'Paco'.",
            isLost = true,
            petType = "Bird",
            lastSeenLocation = "Ramat Gan",
            imageUrl = "https://www.birdland.co.uk/wp-content/uploads/2013/03/Blue-Gold-Macaw-2.jpg",
            createdAt = now - 86400000,
            eventDate = "Apr 03, 2026 at 04:15 PM",
            contactNumber = "050-555-1212"
        )
        val post4 = Post(
            id = UUID.randomUUID().toString(),
            authorId = "user4",
            userName = "Avraham",
            description = "Found a small poodle without a collar.",
            isLost = false,
            petType = "Dog",
            lastSeenLocation = "Jerusalem",
            imageUrl = "https://images.dog.ceo/breeds/poodle-toy/n02113624_9550.jpg",
            createdAt = now,
            eventDate = "Apr 04, 2026 at 01:00 PM",
            contactNumber = "053-444-9999"
        )

        mockPosts = listOf(post1, post2, post3, post4)
    }
}