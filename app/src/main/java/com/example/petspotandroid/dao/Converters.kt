package com.example.petspotandroid.dao

import androidx.room.TypeConverter
import com.example.petspotandroid.data.models.Comment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromCommentList(value: List<Comment>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCommentList(value: String): List<Comment> {
        val listType = object : TypeToken<List<Comment>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
}