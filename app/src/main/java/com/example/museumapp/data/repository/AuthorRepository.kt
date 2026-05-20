package com.example.museumapp.data.repository

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

            if (response.isNotEmpty()) {
                Result.success(response[0])
            } else {
                Result.failure(Exception("Пустой ответ от сервера"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAuthor(id: Int, author: Author): List<Author> {
        val headers = SupabaseClient.getHeaders()
        return api.updateCreator(
            id = id,
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!,
            creator = author
        )
    }

    suspend fun deleteAuthor(id: Int) {
        val headers = SupabaseClient.getHeaders()
        api.deleteCreator(id, headers["apikey"]!!, headers["Authorization"]!!)
    }
}