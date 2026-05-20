package com.example.museumapp.data.repository

import android.util.Log
import com.example.museumapp.data.model.Author
import kotlinx.coroutines.delay

class AuthorRepository {
    private val api = SupabaseClient.apiService

    suspend fun getAllAuthors(): List<Author> {
        val headers = SupabaseClient.getHeaders()
        val response = api.getAllCreators(headers["apikey"]!!, headers["Authorization"]!!)
        return response
    }

    suspend fun searchAuthors(
        name: String? = null
    ): List<Author> {
        val allAuthors = getAllAuthors()
        // Фильтруем локально
        return allAuthors.filter { author ->
            (name.isNullOrEmpty() || author.name.contains(name, ignoreCase = true))
        }
    }

    suspend fun insertAuthor(author: Author): Result<Author> {
        return try {
            val headers = SupabaseClient.getHeaders()
            android.util.Log.d("AUTH_DEBUG", "Is Authenticated: ${com.example.museumapp.data.auth.AuthManager.isAuthenticated()}")
            android.util.Log.d("AUTH_DEBUG", "Auth Header: ${headers["Authorization"]?.take(30)}...") // Первые 30 символов
            val response = api.insertCreator(
                apiKey = headers["apikey"]!!,
                token = headers["Authorization"]!!,
                creator = author
            )

            Result.success(if (response.isNotEmpty()) response[0] else author)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAuthor(
        id: Int,
        name: String,
        biography: String?,
        birthDate: String?,
        deathDate: String?,
        photoUrl: String?
    ): Result<Author> {
        return try {
            val headers = SupabaseClient.getHeaders()
            val updated = api.updateCreatorRpc(
                apiKey = headers["apikey"]!!,
                token = headers["Authorization"]!!,
                params = UpdateCreatorParams(
                    p_creator_id = id,
                    p_name = name,
                    p_biography = biography,
                    p_birth_date = birthDate,
                    p_death_date = deathDate,
                    p_photo_url = photoUrl
                )
            )
            Result.success(updated)
        } catch (e: Exception) {
            Log.e("AuthorRepository", "Update error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteAuthor(id: Int) {
        val headers = SupabaseClient.getHeaders()
        api.deleteCreator(id, headers["apikey"]!!, headers["Authorization"]!!)
    }
}