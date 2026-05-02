package com.example.petspotandroid.base

import android.app.Application
import android.content.Context

class PetSpotApplication : Application() {
    
    companion object {
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}
