package com.example.petspotandroid.data.networking

import com.example.petspotandroid.data.services.AnimalFactApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("accept", "application/json")
            .build()
        return chain.proceed(request)
    }
}

class NetworkClient private constructor() {

    companion object {
        private const val BASE_URL = "https://some-random-api.com/"

        val instance = NetworkClient()

        private val okHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor())
                .build()
        }

        private val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        val animalFactApi: AnimalFactApi by lazy {
            retrofit.create(AnimalFactApi::class.java)
        }
    }
}