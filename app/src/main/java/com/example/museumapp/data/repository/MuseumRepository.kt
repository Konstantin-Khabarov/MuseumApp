package com.example.museumapp.data.repository

import com.example.museumapp.data.auth.AuthManager
import com.example.museumapp.data.model.Museum

class MuseumRepository {

    private val api = SupabaseClient.apiService

    suspend fun getAllMuseums(): List<Museum> {
        val headers = SupabaseClient.getHeaders()
        val response = api.getAllMuseums(headers["apikey"]!!, headers["Authorization"]!!)

        return response
    }

    suspend fun getMuseumById(id: Int): Museum? {
        return try { getAllMuseums().firstOrNull { it.id == id } }
        catch (e: Exception) { null }
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

    suspend fun addMuseum(
        name: String,
        address: String,
        city: String,
        country: String?,
        website: String?
    ): Result<Unit> {
        return try {
            val headers = AuthManager.getApiHeaders()
            val response = api.addMuseumRpc(
                apiKey = headers["apikey"]!!,
                token = headers["Authorization"]!!,
                params = AddMuseumParams(
                    p_name = name,
                    p_address = address,
                    p_city = city,
                    p_country = country,
                    p_website = website
                )
            )
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                return Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMuseum(
        museumId: Int,
        name: String,
        address: String,
        city: String,
        country: String?,
        website: String?
    ): Result<Unit> {
        return try {
            val headers = AuthManager.getApiHeaders()
            val response = api.updateMuseumRpc(
                apiKey = headers["apikey"]!!,
                token = headers["Authorization"]!!,
                params = UpdateMuseumParams(
                    p_museum_id = museumId,
                    p_name = name,
                    p_address = address,
                    p_city = city,
                    p_country = country,
                    p_website = website
                )
            )
            if (!response.isSuccessful) {
                val err = response.errorBody()?.string()
                return Result.failure(Exception("HTTP ${response.code()}: $err"))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMuseum(museumId: Int): Result<Unit> {
        return try {
            val headers = AuthManager.getApiHeaders()
            val response = api.deleteMuseumRpc(
                apiKey = headers["apikey"]!!,
                token = headers["Authorization"]!!,
                params = DeleteMuseumParams(p_museum_id = museumId)
            )
            if (!response.isSuccessful) {
                val err = response.errorBody()?.string()
                return Result.failure(Exception("HTTP ${response.code()}: $err"))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
