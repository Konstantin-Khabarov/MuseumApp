package com.example.museumapp.data.repository

import android.util.Log
import com.example.museumapp.data.auth.AuthManager
import com.example.museumapp.data.model.Author

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

    suspend fun getAuthorById(id: Int): Author? {
        return try {
            val headers = SupabaseClient.getHeaders()
            val result = api.getAllCreators(headers["apikey"]!!, headers["Authorization"]!!)
            result.firstOrNull { it.id == id }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun insertAuthor(author: Author): Result<Author> {
        return try {
            val headers = AuthManager.getApiHeaders()
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
            val headers = AuthManager.getApiHeaders()
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

    suspend fun deleteAuthor(id: Int): Result<Unit> {
        return try {
            val headers = AuthManager.getApiHeaders()
            Log.d("DELETE_AUTHOR", "Starting delete for id=$id")
            Log.d("DELETE_AUTHOR", "apikey present=${headers["apikey"] != null}, token=${headers["Authorization"]?.take(30)}")
            val response = api.deleteCreatorRpc(
                apiKey = headers["apikey"]!!,
                token = headers["Authorization"]!!,
                params = DeleteCreatorParams(p_creator_id = id)
            )
            Log.d("DELETE_AUTHOR", "Response code=${response.code()}, successful=${response.isSuccessful}")
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Log.e("DELETE_AUTHOR", "Error body: $errorBody")
                return Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
            }
            Log.d("DELETE_AUTHOR", "Delete successful")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("DELETE_AUTHOR", "Exception: ${e.javaClass.simpleName}: ${e.message}", e)
            Result.failure(e)
        }
    }
}