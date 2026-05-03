package com.example.petspotandroid.data.services

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

data class AnimalFact(val fact: String)

interface AnimalFactApi {
    @GET("animal/{animalName}")
    fun getFact(@Path("animalName") animal: String): Call<AnimalFact>
}