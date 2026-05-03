package com.example.petspotandroid.model

import com.google.firebase.firestore.Exclude

data class Comment(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorProfileImageUrl: String = "",
    val text: String = "",
    val timestamp: Long = 0L
) {
    companion object {
        fun fromJson(json: Map<String, Any?>): Comment {
            return Comment(
                id = json["id"] as? String ?: "",
                authorId = json["authorId"] as? String ?: "",
                authorName = json["authorName"] as? String ?: "",
                authorProfileImageUrl = json["authorProfileImageUrl"] as? String ?: "",
                text = json["text"] as? String ?: "",
                timestamp = json["timestamp"] as? Long ?: 0L
            )
        }
    }

    @get:Exclude
    val toJson: Map<String, Any?>
        get() = hashMapOf(
            "id" to id,
            "authorId" to authorId,
            "authorName" to authorName,
            "authorProfileImageUrl" to authorProfileImageUrl,
            "text" to text,
            "timestamp" to timestamp
        )
}