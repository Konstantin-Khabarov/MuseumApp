package com.example.museumapp.data.repository

import com.example.museumapp.data.model.Author
import kotlinx.coroutines.delay

// Временный репозиторий, позже заменим на реальный с Room/SQLite
class AuthorRepository {
    private val api = SupabaseClient.apiService

    suspend fun getAllAuthors(): List<Author> {
        val headers = SupabaseClient.getHeaders()
        val response = api.getAllCreators(headers["apikey"]!!, headers["Authorization"]!!)
        //println("DEBUG: Supabase response = $response")
        return response
    }

    suspend fun searchAuthors(
        name: String? = null,
        authorId: Int? = null
    ): List<Author> {
        val allAuthors = getAllAuthors()
        // Фильтруем локально
        return allAuthors.filter { author ->
            ((name.isNullOrEmpty() || author.name.contains(name, ignoreCase = true)) &&
                    (authorId == null || author.id == authorId))
        }
    }

    suspend fun insertAuthor(author: Author): List<Author> {
        val headers = SupabaseClient.getHeaders()
        return api.insertCreator(
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!,
            creator = author
        )
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