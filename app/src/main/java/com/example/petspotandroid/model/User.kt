package com.example.petspotandroid.model

import android.content.Context
import androidx.core.content.edit
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.petspotandroid.base.MyApplication
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

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
        var lastUpdated: Long
            get() {
                return MyApplication.Globals.appContext
                    ?.getSharedPreferences("USERS_PREFS", Context.MODE_PRIVATE)
                    ?.getLong(LAST_UPDATED_KEY, 0) ?: 0
            }
            set(value) {
                MyApplication.Globals.appContext
                    ?.getSharedPreferences("USERS_PREFS", Context.MODE_PRIVATE)
                    ?.edit {
                        putLong(LAST_UPDATED_KEY, value)
                    }
            }

        const val ID_KEY = "id"
        const val FIRST_NAME_KEY = "firstName"
        const val LAST_NAME_KEY = "lastName"
        const val EMAIL_KEY = "email"
        const val PHONE_KEY = "phone"
        const val AVATAR_URL_KEY = "avatarUrl"
        const val CREATED_AT_KEY = "createdAt"
        const val LAST_UPDATED_KEY = "lastUpdated"

        fun fromJson(json: Map<String, Any?>): User {
            val timestamp = json[LAST_UPDATED_KEY] as? Timestamp
            val lastUpdatedLong = timestamp?.toDate()?.time

            return User(
                id = json[ID_KEY] as? String ?: "",
                firstName = json[FIRST_NAME_KEY] as? String ?: "",
                lastName = json[LAST_NAME_KEY] as? String ?: "",
                email = json[EMAIL_KEY] as? String ?: "",
                phone = json[PHONE_KEY] as? String ?: "",
                avatarUrl = json[AVATAR_URL_KEY] as? String,
                createdAt = json[CREATED_AT_KEY] as? Long ?: System.currentTimeMillis(),
                lastUpdated = lastUpdatedLong
            )
        }
    }

    val toJson: Map<String, Any?>
        get() = hashMapOf(
            ID_KEY to id,
            FIRST_NAME_KEY to firstName,
            LAST_NAME_KEY to lastName,
            EMAIL_KEY to email,
            PHONE_KEY to phone,
            AVATAR_URL_KEY to avatarUrl,
            CREATED_AT_KEY to createdAt,
            LAST_UPDATED_KEY to FieldValue.serverTimestamp()
        )
}