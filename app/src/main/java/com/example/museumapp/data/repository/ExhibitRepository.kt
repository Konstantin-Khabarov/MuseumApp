package com.example.museumapp.data.repository

import android.util.Log
import com.example.museumapp.data.model.Exhibit
import com.example.museumapp.data.model.Author
import com.example.museumapp.data.model.Museum
import kotlinx.coroutines.delay

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

        return api.getAllExhibits(
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!,
            range = range  // ← Это критично!
        ).map { it.toExhibitWithRelations() }
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
        return withRetry {
            response.map { rpcExhibit ->
                Exhibit(
                    id = rpcExhibit.exhibit_id,
                    title = rpcExhibit.name,
                    description = rpcExhibit.description,
                    creationYear = rpcExhibit.creation_year,
                    authorId = rpcExhibit.creator_ids?.firstOrNull(), // Берём первого автора
                    museumId = rpcExhibit.museum_id,
                    authorName = rpcExhibit.author_name,
                    museumName = rpcExhibit.museum_name
                    // Можно добавить museumName если нужно
                    //imageUrl = null
                )
            }
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

    private suspend fun <T> withRetry(
        times: Int = 3,  // Максимум 3 попытки
        initialDelay: Long = 100,  // 100 мс перед первой повторной
        maxDelay: Long = 1000,     // Не больше 1 сек между попытками
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        repeat(times - 1) {
            try {
                return block()
            } catch (e: java.net.SocketTimeoutException) {
                Log.w("Repository", "Timeout, retrying in $currentDelay ms...")
                delay(currentDelay)
                currentDelay = (currentDelay * 2).coerceAtMost(maxDelay) // Экспоненциальная задержка
            }
        }
        return block() // Последняя попытка (может выбросить исключение)
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


    suspend fun addExhibit(
        name: String,
        description: String,
        creationYear: Int,
        hallId: Int?,
        authorId: Int?
    ): Exhibit {
        val headers = SupabaseClient.getHeaders()

        // 1. Создаём экспонат
        val exhibitRequest = ExhibitInsertRequest(
            name = name,
            description = description,
            creation_year = creationYear,
            current_hall_id = hallId
        )

        val createdExhibits = api.insertExhibit(
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!,
            prefer = "return=representation",
            exhibit = exhibitRequest
        )

        if (createdExhibits.isEmpty()) {
            throw Exception("Не удалось создать экспонат")
        }

        val createdExhibit = createdExhibits[0]
        val exhibitId = createdExhibit.exhibit_id

        // 2. Если указан автор — создаём связь
        if (authorId != null && authorId > 0) {
            val relationRequest = ExhibitCreatorRequest(
                exhibit_id = exhibitId,
                creator_id = authorId
            )

            api.insertExhibitCreator(
                apiKey = headers["apikey"]!!,
                token = headers["Authorization"]!!,
                prefer = "return=representation",
                relation = relationRequest
            )
        }

        // 3. Возвращаем объект для UI (конвертируем из RPC-ответа)
        return Exhibit(
            id = exhibitId,
            title = createdExhibit.name,
            description = createdExhibit.description,
            creationYear = createdExhibit.creation_year,
            hallId = createdExhibit.current_hall_id,
            authorId = authorId,
            museumId = createdExhibit.museum_id,
            authorName = null,  // Заполнится при следующей загрузке списка
            museumName = createdExhibit.museum_name,
            imageUrl = null
        )
    }

    suspend fun getAuthorsForSpinner(): List<AuthorSpinnerItem> {
        val authors = api.getAllCreators(
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!
        )
        return authors.map { AuthorSpinnerItem(id = it.id, name = it.name) }
    }

    // Добавление нового экспоната
    /*suspend fun insertExhibit(exhibit: Exhibit): List<Exhibit> {
        return api.insertExhibit(
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!,
            exhibit = exhibit.toSupabaseExhibit()
        ).map { it.toExhibitWithRelations() }
    }*/

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
data class AuthorSpinnerItem(
    val id: Int,
    val name: String
) {
    override fun toString(): String = name  // Для отображения в Spinner
}