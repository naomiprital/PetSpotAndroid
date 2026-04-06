package com.example.petspotandroid.data.firebase

import android.graphics.Bitmap
import com.example.petspotandroid.base.StringCompletion
import com.example.petspotandroid.data.models.User
import com.google.firebase.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream

class FirebaseStorageModel {
    private val storage = Firebase.storage

    private fun uploadImage(image: Bitmap, ref: StorageReference, completion: StringCompletion) {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)

        val data = baos.toByteArray()
        val uploadTask = ref.putBytes(data)
        uploadTask.addOnFailureListener {
            completion(null)
        }.addOnSuccessListener { _ ->
            ref.downloadUrl.addOnSuccessListener { uri ->
                completion(uri.toString())
            }.addOnFailureListener {
                completion(null)
            }
        }
    }

    fun uploadUserImage(image: Bitmap, user: User, completion: StringCompletion) {
        val storageRef = storage.reference
        val imagesUserRef = storageRef.child("images/${user.id}/userProfile.jpg")

        uploadImage(image, imagesUserRef, completion)
    }
}