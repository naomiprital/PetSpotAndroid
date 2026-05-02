package com.example.petspotandroid.model

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.petspotandroid.base.MyApplication
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import androidx.core.content.edit

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey
    val id: String,
    val authorId: String,
    val userName: String,
    val authorProfileImageUrl: String?,
    val description: String,
    val imageUrl: String?,
    val createdAt: Long,
    val isLost: Boolean,
    val isResolved: Boolean,
    val petType: String,
    val lastSeenLocation: String,
    val contactNumber: String,
    val eventDate: String,
    var comments: List<Comment> = emptyList(),
    val lastUpdated: Long?

) {

    companion object {
        var lastUpdated: Long
            get() {
                return MyApplication.Globals.appContext
                    ?.getSharedPreferences("POSTS_PREFS", Context.MODE_PRIVATE)
                    ?.getLong(LAST_UPDATED_KEY, 0) ?: 0
            }
            set(value) {
                MyApplication.Globals.appContext
                    ?.getSharedPreferences("POSTS_PREFS", Context.MODE_PRIVATE)
                    ?.edit {
                        putLong(LAST_UPDATED_KEY, value)
                    }
            }

        const val ID_KEY = "id"
        const val AUTHOR_ID_KEY = "authorId"
        const val USER_NAME_KEY = "userName"
        const val AUTHOR_IMAGE_KEY = "authorProfileImageUrl"
        const val DESCRIPTION_KEY = "description"
        const val IMAGE_URL_KEY = "imageUrl"
        const val CREATED_AT_KEY = "createdAt"
        const val IS_LOST_KEY = "isLost"
        const val IS_RESOLVED_KEY = "isResolved"
        const val PET_TYPE_KEY = "petType"
        const val LOCATION_KEY = "lastSeenLocation"
        const val CONTACT_KEY = "contactNumber"
        const val EVENT_DATE_KEY = "eventDate"
        const val LAST_UPDATED_KEY = "lastUpdated"
        const val COMMENTS_KEY = "comments"

        fun fromJson(json: Map<String, Any?>): Post {
            val timestamp = json[LAST_UPDATED_KEY] as? Timestamp
            val lastUpdatedLong = timestamp?.toDate()?.time

            return Post(
                id = json[ID_KEY] as? String ?: "",
                authorId = json[AUTHOR_ID_KEY] as? String ?: "",
                userName = json[USER_NAME_KEY] as? String ?: "",
                authorProfileImageUrl = json[AUTHOR_IMAGE_KEY] as? String,
                description = json[DESCRIPTION_KEY] as? String ?: "",
                imageUrl = json[IMAGE_URL_KEY] as? String,
                createdAt = json[CREATED_AT_KEY] as? Long ?: 0L,
                isLost = json[IS_LOST_KEY] as? Boolean ?: true,
                isResolved = json[IS_RESOLVED_KEY] as? Boolean ?: false,
                petType = json[PET_TYPE_KEY] as? String ?: "",
                lastSeenLocation = json[LOCATION_KEY] as? String ?: "",
                contactNumber = json[CONTACT_KEY] as? String ?: "",
                eventDate = json[EVENT_DATE_KEY] as? String ?: "",
                lastUpdated = lastUpdatedLong
            )
        }
    }

    val toJson: Map<String, Any?>
        get() = hashMapOf(
            ID_KEY to id,
            AUTHOR_ID_KEY to authorId,
            USER_NAME_KEY to userName,
            AUTHOR_IMAGE_KEY to authorProfileImageUrl,
            DESCRIPTION_KEY to description,
            IMAGE_URL_KEY to imageUrl,
            CREATED_AT_KEY to createdAt,
            IS_LOST_KEY to isLost,
            IS_RESOLVED_KEY to isResolved,
            PET_TYPE_KEY to petType,
            LOCATION_KEY to lastSeenLocation,
            CONTACT_KEY to contactNumber,
            EVENT_DATE_KEY to eventDate,
            LAST_UPDATED_KEY to FieldValue.serverTimestamp()
        )
}