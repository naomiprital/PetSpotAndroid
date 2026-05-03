package com.example.petspotandroid.data.models

import android.graphics.Bitmap
import com.example.petspotandroid.base.StringCompletion
import com.example.petspotandroid.model.User

class StorageModel {

    private val firebaseStorage = FirebaseStorageModel()

    fun uploadUserImage(image: Bitmap, user: User, completion: StringCompletion) {
        firebaseStorage.uploadUserImage(image, user, completion)
    }

    fun uploadPostImage(imageBytes: ByteArray, postId: String, completion: (String?) -> Unit) {
        firebaseStorage.uploadPostImage(imageBytes, postId, completion)
    }
}