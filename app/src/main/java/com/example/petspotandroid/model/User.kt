package com.example.petspotandroid.model

import android.content.Context
import androidx.core.content.edit
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.petspotandroid.base.MyApplication
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @get:PropertyName(ID_KEY) @set:PropertyName(ID_KEY)
    var id: String = "",

    @get:PropertyName(FIRST_NAME_KEY) @set:PropertyName(FIRST_NAME_KEY)
    var firstName: String = "",

    @get:PropertyName(LAST_NAME_KEY) @set:PropertyName(LAST_NAME_KEY)
    var lastName: String = "",

    @get:PropertyName(EMAIL_KEY) @set:PropertyName(EMAIL_KEY)
    var email: String = "",

    @get:PropertyName(PHONE_KEY) @set:PropertyName(PHONE_KEY)
    var phone: String = "",

    @get:PropertyName(AVATAR_URL_KEY) @set:PropertyName(AVATAR_URL_KEY)
    var avatarUrl: String? = null,

    @get:PropertyName(CREATED_AT_KEY) @set:PropertyName(CREATED_AT_KEY)
    var createdAt: Long = System.currentTimeMillis(),

    @get:PropertyName(LAST_UPDATED_KEY) @set:PropertyName(LAST_UPDATED_KEY)
    var lastUpdated: Long? = null
) {

    companion object {
        var lastUpdated: Long
            get() = MyApplication.Globals.appContext
                ?.getSharedPreferences("USERS_PREFS", Context.MODE_PRIVATE)
                ?.getLong(LAST_UPDATED_KEY, 0) ?: 0
            set(value) {
                MyApplication.Globals.appContext
                    ?.getSharedPreferences("USERS_PREFS", Context.MODE_PRIVATE)
                    ?.edit { putLong(LAST_UPDATED_KEY, value) }
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
            val rawLastUpdated = json[LAST_UPDATED_KEY]
            val lastUpdatedLong = when (rawLastUpdated) {
                is Timestamp -> rawLastUpdated.toDate().time
                is Long -> rawLastUpdated
                else -> null
            }

            return User(
                id = json[ID_KEY] as? String ?: "",
                firstName = json[FIRST_NAME_KEY] as? String ?: "",
                lastName = json[LAST_NAME_KEY] as? String ?: "",
                email = json[EMAIL_KEY] as? String ?: "",
                phone = json[PHONE_KEY] as? String ?: "",
                avatarUrl = json[AVATAR_URL_KEY] as? String,
                createdAt = (json[CREATED_AT_KEY] as? Long) ?: System.currentTimeMillis(),
                lastUpdated = lastUpdatedLong
            )
        }
    }

    @get:Exclude
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