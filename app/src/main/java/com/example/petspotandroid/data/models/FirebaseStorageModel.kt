package com.example.petspotandroid.data.models

import android.graphics.Bitmap
import android.net.Uri
import com.example.petspotandroid.base.StringCompletion
import com.example.petspotandroid.model.User
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

    fun uploadUserImageFromBitmap(image: Bitmap, user: User, completion: StringCompletion) {
        val storageRef = storage.reference
        val imagesUserRef = storageRef.child("images/${user.id}/userProfile.jpg")

        uploadImage(image, imagesUserRef, completion)
    }

    fun uploadUserImageFromUri(imageUri: Uri, userId: String, completion: StringCompletion) {
        val storageRef = storage.reference
        val imagesUserRef = storageRef.child("images/${userId}/userProfile.jpg")

        val uploadTask = imagesUserRef.putFile(imageUri)

        uploadTask.addOnFailureListener {
            completion(null)
        }.addOnSuccessListener {
            imagesUserRef.downloadUrl.addOnSuccessListener { uri: Uri ->
                completion(uri.toString())
            }.addOnFailureListener {
                completion(null)
            }
        }
    }

    fun uploadPostImage(imageUri: Uri, postId: String, completion: StringCompletion) {
        val storageRef = storage.reference
        val imagesPostRef = storageRef.child("post_images/${postId}.jpg")

        val uploadTask = imagesPostRef.putFile(imageUri)

        uploadTask.addOnFailureListener { _ ->
            completion(null)
        }.addOnSuccessListener {
            imagesPostRef.downloadUrl.addOnSuccessListener { uri ->
                completion(uri.toString())
            }.addOnFailureListener { _ ->
                completion(null)
            }
        }
    }
}
