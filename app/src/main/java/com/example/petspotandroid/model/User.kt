package com.example.petspotandroid.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val avatarUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    var lastUpdated: Long? = null
) {
    companion object {
        const val COLLECTION_NAME = "users"
        const val LAST_UPDATED = "lastUpdated"

        fun fromJson(json: Map<String, Any>): User {
            val id = json["id"] as? String ?: ""
            val firstName = json["firstName"] as? String ?: ""
            val lastName = json["lastName"] as? String ?: ""
            val email = json["email"] as? String ?: ""
            val phone = json["phone"] as? String ?: ""
            val avatarUrl = json["avatarUrl"] as? String
            val createdAt = json["createdAt"] as? Long ?: 0L
            val lastUpdated = json[LAST_UPDATED] as? Long ?: 0L
            
            return User(id, firstName, lastName, email, phone, avatarUrl, createdAt, lastUpdated)
        }
    }

    fun toJson(): Map<String, Any> {
        val json = mutableMapOf<String, Any>()
        json["id"] = id
        json["firstName"] = firstName
        json["lastName"] = lastName
        json["email"] = email
        json["phone"] = phone
        avatarUrl?.let { json["avatarUrl"] = it }
        json["createdAt"] = createdAt
        lastUpdated?.let { json[LAST_UPDATED] = it }
        return json
    }
}
