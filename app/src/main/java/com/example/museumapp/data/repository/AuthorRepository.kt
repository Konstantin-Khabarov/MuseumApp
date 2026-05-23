package com.example.museumapp.data.repository

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
            val request = AuthorInsertRequest(
                name = author.name,
                biography = author.biography,
                birth_date = author.birthDate,
                death_date = author.deathDate,
                photo_url = author.photoUrl
            )
            val response = api.insertCreatorRaw(
                apiKey = headers["apikey"]!!,
                token = headers["Authorization"]!!,
                creator = request
            )
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                return Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
            }
            val body = response.body()
            Result.success(if (!body.isNullOrEmpty()) body[0] else author)
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
            Result.failure(e)
        }
    }

    suspend fun deleteAuthor(id: Int): Result<Unit> {
        return try {
            val headers = AuthManager.getApiHeaders()
            val response = api.deleteCreatorRpc(
                apiKey = headers["apikey"]!!,
                token = headers["Authorization"]!!,
                params = DeleteCreatorParams(p_creator_id = id)
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
}
