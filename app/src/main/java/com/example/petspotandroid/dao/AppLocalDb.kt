package com.example.petspotandroid.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.petspotandroid.data.models.Post
import com.example.petspotandroid.data.models.User

@Database(entities = [Post::class, User::class], version = 6, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppLocalDb : RoomDatabase() {

    abstract fun postDao(): PostDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppLocalDb? = null

        fun getDatabase(context: Context): AppLocalDb {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppLocalDb::class.java,
                    "pet_spot_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}