package com.example.museumapp.data.repository

import com.example.museumapp.data.model.Author
import kotlinx.coroutines.delay

// Временный репозиторий, позже заменим на реальный с Room/SQLite
class AuthorRepository {

    private val mockAuthors = listOf(
        Author(1, "Иван", "био 1", "1980-01-15"),
        Author(2, "Анна", "био 2", "1975-03-22", "1995-03-22"),
        Author(3, "Сергей", "био 3", "1990-11-05"),
        Author(4, "Мария", "био 4", "1905-07-30", "1990-11-05"),
        Author(5, "Алексей", "био 5", "1850-09-18", "1890-11-05")
    )

    suspend fun searchAuthors(
        name: String? = null,
        authorId: Int? = null
    ): List<Author> {

        return mockAuthors.filter { author ->
            (name.isNullOrEmpty() || author.name.contains(name, ignoreCase = true)) &&
                    (authorId == null || author.id == authorId)
        }
    }

    suspend fun addAuthor(author: Author) {
        // Позже добавим реальную логику
        delay(500)
    }

    suspend fun updateAuthor(author: Author) {
        // Позже добавим реальную логику
        delay(500)
    }
}