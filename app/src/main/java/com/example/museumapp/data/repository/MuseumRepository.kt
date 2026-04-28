package com.example.museumapp.data.repository

import com.example.museumapp.data.model.Museum

class MuseumRepository {

    private val api = SupabaseClient.apiService

    suspend fun getAllMuseums(): List<Museum> {
        val headers = SupabaseClient.getHeaders()
        val response = api.getAllMuseums(headers["apikey"]!!, headers["Authorization"]!!)
        //println("DEBUG: Supabase response = $response")
        return response
    }

    /*private val mockMuseums = listOf(
        Museum(
            id = 1,
            name = "Лувр",
            address = "Rue de Rivoli, 75001 Paris, France",
            country = "Франция"
        ),
        Museum(
            id = 2,
            name = "Эрмитаж",
            address = "Дворцовая площадь, 2, Санкт-Петербург, Россия",
            country = "Россия"
        ),
        Museum(
            id = 3,
            name = "Британский музей",
            address = "Great Russell St, London WC1B 3DG, UK",
            country = "Великобритания"
        ),
        Museum(
            id = 4,
            name = "Метрополитен-музей",
            address = "1000 5th Ave, New York, NY 10028, USA",
            country = "США"
        ),
        Museum(
            id = 5,
            name = "Прадо",
            address = "Calle de Ruiz de Alarcón, 23, 28014 Madrid, Spain",
            country = "Испания"
        )
    )*/

    suspend fun searchMuseums(
        name: String? = null,
        city: String? = null
    ): List<Museum> {
        val allMuseums = getAllMuseums()
        return allMuseums.filter { museum ->
            var matches = true
            if (!name.isNullOrEmpty()) {
                matches = matches && museum.name.contains(name, ignoreCase = true)
            }
            if (!city.isNullOrEmpty()) {
                matches = matches && museum.city.contains(city, ignoreCase = true)
            }
            matches
        }
    }

    suspend fun addMuseum(museum: Museum) {
        // Логика добавления музея
    }

    suspend fun updateMuseum(museum: Museum) {
        // Логика обновления музея
    }

    suspend fun deleteMuseum(museumId: Int) {
        // Логика удаления музея
    }
}