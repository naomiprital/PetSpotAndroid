package com.example.petspotandroid.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

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
    var isResolved: Boolean = false,
    var petType: String = "",
    var lastSeenLocation: String = "",
    var contactNumber: String = "",
    var eventDate: String = "",
    var comments: List<Comment> = emptyList()
) : Serializable