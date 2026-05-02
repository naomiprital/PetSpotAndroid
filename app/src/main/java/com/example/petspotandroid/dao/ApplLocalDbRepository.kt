package com.example.petspotandroid.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.petspotandroid.model.Post
import com.example.petspotandroid.model.User

@Database(entities = [User::class, Post::class], version = 4)
@TypeConverters(Converters::class)
abstract class AppLocalDbRepository: RoomDatabase() {
    abstract val userDao: UserDao
    abstract val postDao: PostDao
}