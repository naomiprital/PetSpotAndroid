package com.example.petspotandroid.data.repository.fact

import com.example.petspotandroid.data.networking.NetworkClient
import com.example.petspotandroid.data.services.AnimalFact
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class FactRepository private constructor() {

    private val db = FirebaseFirestore.getInstance()
    private val factRef = db.collection("daily_facts").document("current_fact")

    companion object {
        val instance = FactRepository()
    }

    fun getDailyFact(supportedAnimals: List<String>, completion: (String?) -> Unit) {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        factRef.get().addOnSuccessListener { document ->
            val savedDate = document.getString("date")
            val savedFact = document.getString("fact")

            if (savedDate == todayDate && savedFact != null) {
                completion(savedFact)
            } else {
                fetchNewFact(supportedAnimals, todayDate, completion)
            }
        }.addOnFailureListener {
            completion(null)
        }
    }

    private fun fetchNewFact(supportedAnimals: List<String>, date: String, completion: (String?) -> Unit) {
        val randomAnimal = supportedAnimals.random()

        NetworkClient.animalFactApi.getFact(randomAnimal).enqueue(object : Callback<AnimalFact> {
            override fun onResponse(call: Call<AnimalFact>, response: Response<AnimalFact>) {
                val newFact = response.body()?.fact
                if (newFact != null) {
                    val newFactData = mapOf(
                        "date" to date,
                        "fact" to newFact
                    )
                    factRef.set(newFactData)
                    completion(newFact)
                } else {
                    completion(null)
                }
            }

            override fun onFailure(call: Call<AnimalFact>, t: Throwable) {
                completion(null)
            }
        })
    }
}