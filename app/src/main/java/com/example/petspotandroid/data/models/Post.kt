package com.example.petspotandroid.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey
    var id: String = "",
    var authorId: String = "",
    var userName: String = "",
    val authorProfileImageUrl: String = "",
    var description: String = "",
    var imageUrl: String = "",
    var createdAt: Long = 0L,
    var isLost: Boolean = true,
    var petType: String = "",
    var lastSeenLocation: String = "",
    var contactNumber: String = "",
    var eventDate: String = "",
)