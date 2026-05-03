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

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey
    @get:PropertyName(ID_KEY) @set:PropertyName(ID_KEY)
    var id: String = "",

    @get:PropertyName(AUTHOR_ID_KEY) @set:PropertyName(AUTHOR_ID_KEY)
    var authorId: String = "",

    @get:PropertyName(USER_NAME_KEY) @set:PropertyName(USER_NAME_KEY)
    var userName: String = "",

    @get:PropertyName(AUTHOR_IMAGE_KEY) @set:PropertyName(AUTHOR_IMAGE_KEY)
    var authorProfileImageUrl: String? = null,

    @get:PropertyName(DESCRIPTION_KEY) @set:PropertyName(DESCRIPTION_KEY)
    var description: String = "",

    @get:PropertyName(IMAGE_URL_KEY) @set:PropertyName(IMAGE_URL_KEY)
    var imageUrl: String? = null,

    @get:PropertyName(CREATED_AT_KEY) @set:PropertyName(CREATED_AT_KEY)
    var createdAt: Long = 0L,

    @get:PropertyName(IS_LOST_KEY) @set:PropertyName(IS_LOST_KEY)
    var isLost: Boolean = true,

    @get:PropertyName(IS_RESOLVED_KEY) @set:PropertyName(IS_RESOLVED_KEY)
    var isResolved: Boolean = false,

    @get:PropertyName(PET_TYPE_KEY) @set:PropertyName(PET_TYPE_KEY)
    var petType: String = "",

    @get:PropertyName(LOCATION_KEY) @set:PropertyName(LOCATION_KEY)
    var lastSeenLocation: String = "",

    @get:PropertyName(CONTACT_KEY) @set:PropertyName(CONTACT_KEY)
    var contactNumber: String = "",

    @get:PropertyName(EVENT_DATE_KEY) @set:PropertyName(EVENT_DATE_KEY)
    var eventDate: String = "",

    var comments: List<Comment> = emptyList(),

    @get:PropertyName(LAST_UPDATED_KEY) @set:PropertyName(LAST_UPDATED_KEY)
    var lastUpdated: Long? = null
) {
    companion object {
        var lastUpdated: Long
            get() = MyApplication.Globals.appContext
                ?.getSharedPreferences("POSTS_PREFS", Context.MODE_PRIVATE)
                ?.getLong(LAST_UPDATED_KEY, 0) ?: 0
            set(value) {
                MyApplication.Globals.appContext
                    ?.getSharedPreferences("POSTS_PREFS", Context.MODE_PRIVATE)
                    ?.edit { putLong(LAST_UPDATED_KEY, value) }
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

            val commentsJson = json[COMMENTS_KEY] as? List<Map<String, Any?>> ?: emptyList()
            val commentsList = commentsJson.map { Comment.fromJson(it) }

            return Post(
                id = json[ID_KEY] as? String ?: "",
                authorId = json[AUTHOR_ID_KEY] as? String ?: "",
                userName = json[USER_NAME_KEY] as? String ?: "",
                authorProfileImageUrl = json[AUTHOR_IMAGE_KEY] as? String,
                description = json[DESCRIPTION_KEY] as? String ?: "",
                imageUrl = json[IMAGE_URL_KEY] as? String,
                createdAt = (json[CREATED_AT_KEY] as? Long) ?: 0L,
                isLost = (json[IS_LOST_KEY] as? Boolean) ?: (json["lost"] as? Boolean) ?: true,
                isResolved = (json[IS_RESOLVED_KEY] as? Boolean) ?: (json["resolved"] as? Boolean) ?: false,
                petType = json[PET_TYPE_KEY] as? String ?: "",
                lastSeenLocation = json[LOCATION_KEY] as? String ?: "",
                contactNumber = json[CONTACT_KEY] as? String ?: "",
                eventDate = json[EVENT_DATE_KEY] as? String ?: "",
                comments = commentsList,
                lastUpdated = lastUpdatedLong
            )
        }
    }

    @get:Exclude
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
            COMMENTS_KEY to comments.map { it.toJson },
            LAST_UPDATED_KEY to FieldValue.serverTimestamp()
        )
}