package com.example.petspotandroid.base

import com.example.petspotandroid.model.Post
import com.example.petspotandroid.model.User

typealias StringCompletion = (String?) -> Unit
typealias FirebaseAuthCompletion = (success: Boolean, error: String?) -> Unit

typealias FirestoreCompletion = (success: Boolean, error: String?) -> Unit
typealias FirestoreUserCompletion = (user: User?, error: String?) -> Unit
typealias FirestorePostsCompletion = (posts: List<Post>) -> Unit