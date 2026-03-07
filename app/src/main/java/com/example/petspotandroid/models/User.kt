package com.example.petspotandroid.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String, // String ID to match Firebase later
    val name: String,
    val email: String,
    val avatarUrl: String? = null
)