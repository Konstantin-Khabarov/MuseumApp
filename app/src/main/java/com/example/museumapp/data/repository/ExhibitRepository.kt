package com.example.museumapp.data.repository

import com.example.museumapp.data.model.Exhibit
import com.example.museumapp.data.model.Author
import com.example.museumapp.data.model.Museum

class ExhibitRepository {
    private val api = SupabaseClient.apiService
    private val headers = SupabaseClient.getHeaders()

    companion object {
        private const val PAGE_SIZE = 20
    }

    suspend fun getExhibitsPage(page: Int, pageSize: Int = PAGE_SIZE): List<Exhibit> {
        val start = page * pageSize
        val end = start + pageSize - 1
        val range = "$start-$end"

        val response = api.getAllExhibits(
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!,
            range = range
        )

        println("DEBUG: Loaded exhibits page $page ($range) = ${response.size} items")
        return response.map { it.toExhibitWithRelations() }
    }

    suspend fun searchExhibits(
        title: String?,
        authorName: String?,
        museumName: String?,
        page: Int = 0,
        pageSize: Int = PAGE_SIZE
    ): List<Exhibit> {
        val start = page * pageSize
        val end = start + pageSize - 1
        val range = "$start-$end"

        val params = SearchParams(
            title_filter = title?.takeIf { it.isNotBlank() },
            author_name_filter = authorName?.takeIf { it.isNotBlank() },
            museum_name_filter = museumName?.takeIf { it.isNotBlank() },
            result_limit = pageSize,
            result_offset = start
        )

        println("DEBUG: Search RPC call - title=$title, author=$authorName, museum=$museumName, range=$range")

        val response = api.searchExhibitsRpc(
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!,
            range = range,
            params = params
        )

        println("DEBUG: Search RPC response = ${response.size} items")

        // Конвертируем RPC ответ в модель Exhibit
        return response.map { rpcExhibit ->
            Exhibit(
                id = rpcExhibit.exhibit_id,
                title = rpcExhibit.name,
                description = rpcExhibit.description,
                creationYear = rpcExhibit.creation_year,
                authorId = rpcExhibit.creator_ids?.firstOrNull(), // Берём первого автора
                museumId = rpcExhibit.museum_id,
                // Можно добавить museumName если нужно
                //imageUrl = null
            )
        }
    }

    suspend fun loadNextPage(
        currentQuery: String? = null,
        currentAuthor: String? = null,
        currentMuseum: String? = null,
        nextPage: Int
    ): List<Exhibit> {
        return searchExhibits(currentQuery, currentAuthor, currentMuseum, nextPage)
    }
    // Получение всех экспонатов
    suspend fun getAllExhibits(): List<Exhibit> {
        val headers = SupabaseClient.getHeaders()
        val response = api.getAllExhibits(
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!
        )
        println("DEBUG: Supabase response = $response")
        return response
    }

    suspend fun getAllAuthors(): List<Author> {
        return api.getAllCreators(
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!
        )
    }

    /**
     * Получение всех музеев (для справочников)
     */
    suspend fun getAllMuseums(): List<Museum> {
        return api.getAllMuseums(
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!
        )
    }

    /**
     * Получение всех залов (для справочников)
     */
    /*suspend fun getAllHalls(): List<Hall> {
        return api.getAllHalls(
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!
        )
    }*/

    // Поиск экспонатов по различным критериям
    /*suspend fun searchExhibits(
        title: String? = null,
        authorName: String? = null,
        museumName: String? = null
    ): List<Exhibit> {
        val allExhibits = getAllExhibits()

        // Если нет критериев поиска, возвращаем все экспонаты
        if (title.isNullOrEmpty() && authorName.isNullOrEmpty() && museumName.isNullOrEmpty()) {
            return allExhibits
        }

        // Получаем всех авторов и музеи для поиска по именам
        val authors = getAllAuthors()
        val museums = getAllMuseums()

        return allExhibits.filter { exhibit ->
            var matches = true

            // Поиск по названию экспоната
            if (!title.isNullOrEmpty()) {
                matches = matches && exhibit.title.contains(title, ignoreCase = true)
            }

            // Поиск по имени автора
            if (!authorName.isNullOrEmpty()) {
                val author = authors.find { it.id == exhibit.authorId }
                val authorMatches = author?.name?.contains(authorName, ignoreCase = true) ?: false
                matches = matches && authorMatches
            }

            // Поиск по названию музея
            if (!museumName.isNullOrEmpty()) {
                val museum = museums.find { it.id == exhibit.museumId }
                val museumMatches = museum?.name?.contains(museumName, ignoreCase = true) ?: false
                matches = matches && museumMatches
            }

            matches
        }
    }*/

    // Поиск экспонатов по ID автора
    suspend fun getExhibitsByAuthorId(authorId: Int): List<Exhibit> {
        val allExhibits = getAllExhibits()
        return allExhibits.filter { it.authorId == authorId }
    }

    // Поиск экспонатов по ID музея
    suspend fun getExhibitsByMuseumId(museumId: Int): List<Exhibit> {
        val allExhibits = getAllExhibits()
        return allExhibits.filter { it.museumId == museumId }
    }

    // Добавление нового экспоната
    suspend fun insertExhibit(exhibit: Exhibit): List<Exhibit> {
        return api.insertExhibit(
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!,
            exhibit = exhibit.toSupabaseExhibit()
        ).map { it.toExhibitWithRelations() }
    }

    // Обновление экспоната
    suspend fun updateExhibit(id: Int, exhibit: Exhibit): List<Exhibit> {
        return api.updateExhibit(
            id = id,
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!,
            exhibit = exhibit.toSupabaseExhibit()
        ).map { it.toExhibitWithRelations() }
    }

    // Удаление экспоната
    suspend fun deleteExhibit(id: Int) {
        val headers = SupabaseClient.getHeaders()
        api.deleteExhibit(
            id = id,
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!
        )
    }

    private fun Exhibit.toSupabaseExhibit(): Exhibit {
        return this
    }

    // Конвертация с добавлением связанных данных (если нужно)
    private fun Exhibit.toExhibitWithRelations(): Exhibit {
        return this
    }
}