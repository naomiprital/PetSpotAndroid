package com.example.petspotandroid.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.petspotandroid.model.User

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: String): LiveData<User?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserByIdSync(id: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)
}
