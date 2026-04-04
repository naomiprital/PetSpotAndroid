package com.example.petspotandroid.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey
    val id: String,
    val ownerId: String,
    val userName: String,
    val description: String,
    val imageUrl: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isLost: Boolean = true,
    val petType: String = "Dog",
    val lastSeenLocation: String = ""
)