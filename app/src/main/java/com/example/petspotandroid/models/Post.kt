package com.example.petspotandroid.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey
    val id: String,
    val authorId: String = "example_user", // TODO: Change to real author userId
    val userName: String = "Example User", // TODO: Change to real author username
    val description: String,
    val imageUrl: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isLost: Boolean = true,
    val petType: String = "Dog",
    val lastSeenLocation: String = "",
    val contactNumber: String = "",
    val eventDate: String = ""
)