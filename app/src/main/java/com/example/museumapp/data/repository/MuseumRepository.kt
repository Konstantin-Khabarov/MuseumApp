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