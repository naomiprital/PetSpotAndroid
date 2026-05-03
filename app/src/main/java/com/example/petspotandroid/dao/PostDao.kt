package com.example.petspotandroid.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.petspotandroid.model.Post

@Dao
interface PostDao {
    @Query("SELECT * FROM posts WHERE isResolved = 0 ORDER BY createdAt DESC")
    fun getAllPosts(): LiveData<List<Post>>

    @Query("SELECT * FROM posts")
    fun getAllPostsSync(): List<Post>

    @Update
    fun updatePost(post: Post)

    @Query("SELECT * FROM posts WHERE authorId = :userId ORDER BY createdAt DESC")
    fun getPostsByUser(userId: String): LiveData<List<Post>>

    @Query("SELECT * FROM posts WHERE id = :postId")
    fun getPostById(postId: String): LiveData<Post>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPost(posts: Post)

    @Delete
    fun delete(post: Post)

    @Query("UPDATE posts SET userName = :newName, authorProfileImageUrl = :newAvatarUrl WHERE authorId = :authorId")
    fun updateAuthorMetadata(authorId: String, newName: String, newAvatarUrl: String?)
}