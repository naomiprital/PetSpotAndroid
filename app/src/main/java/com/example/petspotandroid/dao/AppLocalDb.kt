package com.example.petspotandroid.dao

import androidx.room.Room
import com.example.petspotandroid.base.MyApplication

object AppLocalDB {
    val db: AppLocalDbRepository by lazy {

        val context = MyApplication.appContext
            ?: throw IllegalStateException("Context is null")

        Room.databaseBuilder(
            context = context,
            klass = AppLocalDbRepository::class.java,
            name = "petspot.db"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }
}