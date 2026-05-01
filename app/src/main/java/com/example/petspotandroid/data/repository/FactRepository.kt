package com.example.petspotandroid.data.repository

import com.example.petspotandroid.api.RetrofitInstance
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FactRepository {

    private val db = FirebaseFirestore.getInstance()
    private val factRef = db.collection("daily_facts").document("current_fact")

    suspend fun getDailyFact(supportedAnimals: List<String>): String? {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        return try {
            val document = factRef.get().await()
            val savedDate = document.getString("date")
            val savedFact = document.getString("fact")

            if (savedDate == todayDate && savedFact != null) {
                savedFact
            } else {
                val randomAnimal = supportedAnimals.random()
                val response = RetrofitInstance.api.getFact(randomAnimal)

                val newFactData = mapOf(
                    "date" to todayDate,
                    "fact" to response.fact
                )
                factRef.set(newFactData).await()
                response.fact
            }
        } catch (exception: Exception) {
            null
        }
    }
}