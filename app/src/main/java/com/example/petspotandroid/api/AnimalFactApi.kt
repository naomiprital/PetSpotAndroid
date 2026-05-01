package com.example.petspotandroid.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

data class AnimalFact(val fact: String)

interface AnimalFactApi {
    @GET("animal/{animalName}")
    suspend fun getFact(@Path("animalName") animal: String): AnimalFact
}

object RetrofitInstance {
    val api: AnimalFactApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://some-random-api.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AnimalFactApi::class.java)
    }
}