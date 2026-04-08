package com.example.petspotandroid.data.models

data class Comment(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorProfileImageUrl: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)