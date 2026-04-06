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

    fun uploadUserImage(image: Bitmap, user: User, completion: StringCompletion) {

        val storageRef = storage.reference
        val mainRef = storageRef.child("mountains.jpg")
        val imagesRef = storageRef.child("images/mountains.jpg")
        val imagesUserRef = storageRef.child("images/${user.id}/mountains.jpg")

        uploadImage(image, imagesUserRef, completion)
    }

    private fun uploadImage(image: Bitmap, ref: StorageReference, completion: StringCompletion) {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = ref.putBytes(data)
        uploadTask.addOnFailureListener {
            completion(null)
        }.addOnSuccessListener { taskSnapshot ->
            ref.downloadUrl.addOnSuccessListener { uri ->
                completion(uri.toString())
            }.addOnFailureListener {
                completion(null)
            }
        }
    }
}