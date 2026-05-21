package com.example.museumapp.data.repository

import android.util.Log
import com.example.museumapp.data.auth.AuthManager
import com.example.museumapp.data.cache.DataCache
import com.example.museumapp.data.model.Exhibit
import com.example.museumapp.data.model.Author
import com.example.museumapp.data.model.Hall
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
        val range = "$start-${start + pageSize - 1}"

        Log.d("EXHIBIT_LOAD", "getExhibitsPage: page=$page range=$range")
        val rpcResponse = try {
            api.searchExhibitsRpc(
                apiKey = headers["apikey"]!!,
                token = headers["Authorization"]!!,
                range = range,
                params = SearchParams(result_limit = pageSize, result_offset = start)
            )
        } catch (e: retrofit2.HttpException) {
            val body = e.response()?.errorBody()?.string()
            Log.e("EXHIBIT_LOAD", "HTTP ${e.code()} on searchExhibitsRpc: $body")
            throw e
        } catch (e: Exception) {
            Log.e("EXHIBIT_LOAD", "${e.javaClass.simpleName}: ${e.message}", e)
            throw e
        }

        val exhibits = rpcResponse.map { rpc ->
            Exhibit(
                id = rpc.exhibit_id,
                title = rpc.name,
                description = rpc.description,
                creationYear = rpc.creation_year,
                hallId = rpc.current_hall_id,
                museumId = rpc.museum_id,
                authorId = rpc.creator_ids?.firstOrNull(),
                authorName = rpc.author_name,
                museumName = rpc.museum_name,
                imageUrl = rpc.image_url,
                hallNumber = rpc.hall_number
            )
        }

        DataCache.cacheExhibits(exhibits, clearPrevious = (page == 0))
        return exhibits
    }

    suspend fun searchExhibits(
        title: String?,
        authorName: String?,
        museumName: String?,
        page: Int = 0,
        pageSize: Int = PAGE_SIZE
    ): List<Exhibit> {
        val start = page * pageSize
        val range = "$start-${start + pageSize - 1}"

        val params = SearchParams(
            title_filter = title?.takeIf { it.isNotBlank() },
            author_name_filter = authorName?.takeIf { it.isNotBlank() },
            museum_name_filter = museumName?.takeIf { it.isNotBlank() },
            result_limit = pageSize,
            result_offset = start
        )

        return try {
            Log.d("EXHIBIT_LOAD", "searchExhibits: title=$title author=$authorName museum=$museumName")

            val rpcResponse = try {
                api.searchExhibitsRpc(
                    apiKey = headers["apikey"]!!,
                    token = headers["Authorization"]!!,
                    range = range,
                    params = params
                )
            } catch (e: retrofit2.HttpException) {
                val body = e.response()?.errorBody()?.string()
                Log.e("EXHIBIT_LOAD", "HTTP ${e.code()} on searchExhibitsRpc: $body")
                throw e
            }

            Log.d("IMG_DEBUG", "searchExhibits RPC returned ${rpcResponse.size} items")
            rpcResponse.forEach { rpc ->
                Log.d("IMG_DEBUG", "  rpc id=${rpc.exhibit_id} name='${rpc.name}' image_url=${rpc.image_url}")
            }

            val exhibits = rpcResponse.map { rpc ->
                Exhibit(
                    id = rpc.exhibit_id,
                    title = rpc.name,
                    description = rpc.description,
                    creationYear = rpc.creation_year,
                    hallId = rpc.current_hall_id,
                    museumId = rpc.museum_id,
                    authorId = rpc.creator_ids?.firstOrNull(),
                    authorName = rpc.author_name,
                    museumName = rpc.museum_name,
                    imageUrl = rpc.image_url,
                    hallNumber = rpc.hall_number
                )
            }

            // 🔥 Сохраняем в DataCache
            DataCache.cacheExhibits(exhibits, clearPrevious = (page == 0))
            Log.d("Repository", "Cached ${exhibits.size} exhibits in DataCache")

            exhibits

        } catch (e: Exception) {
            Log.e("Repository", "Search failed", e)
            emptyList()
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

    private suspend fun <T> withRetry(
        times: Int = 2,  // Максимум 3 попытки
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


    suspend fun getExhibitsByCreator(creatorId: Int): List<Exhibit> {
        val rpc = api.getExhibitsByCreatorRpc(
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!,
            params = mapOf("p_creator_id" to creatorId)
        )
        return rpc.map { r ->
            Exhibit(
                id = r.exhibit_id,
                title = r.name,
                description = r.description,
                creationYear = r.creation_year,
                hallId = r.current_hall_id,
                museumId = r.museum_id,
                authorId = creatorId,
                authorName = r.author_name,
                museumName = r.museum_name,
                imageUrl = r.image_url,
                hallNumber = r.hall_number
            )
        }
    }

    suspend fun addExhibit(
        name: String,
        description: String,
        creationYear: Int,
        hallId: Int?,
        authorId: Int?,
        imageUrl: String? = null
    ): Exhibit {
        val authHeaders = AuthManager.getApiHeaders()
        Log.d("ADD_EXHIBIT", "token=${authHeaders["Authorization"]?.take(40)}")
        Log.d("ADD_EXHIBIT", "params: name=$name, year=$creationYear, hallId=$hallId, authorId=$authorId, imageUrl=$imageUrl")

        val exhibitId = try {
            api.addExhibitRpc(
                apiKey = authHeaders["apikey"]!!,
                token = authHeaders["Authorization"]!!,
                params = AddExhibitParams(
                    p_name = name,
                    p_description = description,
                    p_creation_year = creationYear,
                    p_hall_id = hallId,
                    p_author_id = authorId,
                    p_image_url = imageUrl
                )
            ).also { Log.d("ADD_EXHIBIT", "RPC success, new exhibit_id=$it") }
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("ADD_EXHIBIT", "HTTP ${e.code()} error body: $errorBody")
            throw e
        } catch (e: Exception) {
            Log.e("ADD_EXHIBIT", "Exception: ${e.javaClass.simpleName}: ${e.message}", e)
            throw e
        }

        return Exhibit(
            id = exhibitId,
            title = name,
            description = description,
            creationYear = creationYear,
            hallId = hallId,
            authorId = authorId,
            museumId = null,
            authorName = null,
            museumName = null,
            imageUrl = imageUrl
        )
    }

    suspend fun getAuthorsForSpinner(): List<AuthorSpinnerItem> {
        return DataCache.getAuthors {
        val authors = api.getAllCreators(
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!
        )
        authors.map { AuthorSpinnerItem(id = it.id, name = it.name) }
        }
    }
    suspend fun getMuseumsForSpinner(): List<MuseumSpinnerItem> {
        return DataCache.getMuseums {
            val museums = api.getAllMuseums(
                apiKey = headers["apikey"]!!,
                token = headers["Authorization"]!!
            )
            museums.map { MuseumSpinnerItem(id = it.id, name = it.name) }
        }
    }

    suspend fun getHallsByMuseumId(museumId: Int): List<Hall> {
        return api.getHallsByMuseumId(
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!,
            museumIdFilter = "eq.$museumId"  // ← Форматируем как "eq.123"
        ).sortedBy { it.hall_number?.toIntOrNull() ?: Int.MAX_VALUE }
    }

    // Обновление экспоната
    suspend fun updateExhibit(
        exhibitId: Int,
        name: String,
        description: String,
        creationYear: Int,
        hallId: Int?,
        authorId: Int?,
        imageUrl: String? = null
    ): Result<Exhibit> {
        return try {
            val authHeaders = AuthManager.getApiHeaders()
            val params = UpdateExhibitParams(
                p_exhibit_id = exhibitId, p_name = name, p_description = description,
                p_creation_year = creationYear, p_hall_id = hallId, p_author_id = authorId, p_image_url = imageUrl
            )
            Log.d("UPDATE_EXHIBIT", "params: $params")
            Log.d("UPDATE_EXHIBIT", "token=${authHeaders["Authorization"]?.take(40)}")

            val updated = try {
                api.updateExhibitRpc(
                    apiKey = authHeaders["apikey"]!!,
                    token = authHeaders["Authorization"]!!,
                    params = params
                )
            } catch (e: retrofit2.HttpException) {
                val body = e.response()?.errorBody()?.string()
                Log.e("UPDATE_EXHIBIT", "HTTP ${e.code()} error body: $body")
                throw e
            }

            val updatedExhibit = Exhibit(
                id = updated.exhibit_id,
                title = updated.name,
                description = updated.description,
                creationYear = updated.creation_year,
                hallId = updated.current_hall_id,
                museumId = updated.museum_id,
                authorId = updated.creator_ids?.firstOrNull(),
                authorName = updated.author_name,
                museumName = updated.museum_name
            )

            Log.d("ExhibitRepository", "Successfully updated exhibit $exhibitId")
            Result.success(updatedExhibit)
        } catch (e: Exception) {
            Log.e("ExhibitRepository", "Update error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Удаление экспоната
    suspend fun deleteExhibit(exhibitId: Int): Result<Unit> {
        return try {
            Log.d("ExhibitRepository", "Deleting exhibit $exhibitId via RPC...")
            val authHeaders = AuthManager.getApiHeaders()

            val result = api.deleteExhibitRpc(
                apiKey = authHeaders["apikey"]!!,
                token = authHeaders["Authorization"]!!,
                params = DeleteExhibitParams(p_exhibit_id = exhibitId)
            )

            if (result) {
                Log.d("ExhibitRepository", "Successfully deleted exhibit $exhibitId")
                Result.success(Unit)
            } else {
                Log.e("ExhibitRepository", "Exhibit $exhibitId not found")
                Result.failure(Exception("Exhibit not found"))
            }
        } catch (e: Exception) {
            Log.e("ExhibitRepository", "Delete error: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun Exhibit.toSupabaseExhibit(): Exhibit {
        return this
    }

    // В ExhibitRepository.kt
    suspend fun getExhibitById(exhibitId: Int): Result<Exhibit> {
        return try {
            // 🔥 1. Пробуем получить из кэша
            val cached = DataCache.getExhibit(exhibitId) { null } // loader = null, т.к. не загружаем автоматически

            if (cached != null) {
                Log.d("IMG_DEBUG", "getExhibitById: CACHE HIT id=$exhibitId imageUrl=${cached.imageUrl}")
                return Result.success(cached)
            }

            Log.d("IMG_DEBUG", "getExhibitById: CACHE MISS id=$exhibitId — fetching via RPC")

            val details = api.getExhibitDetailsRpc(
                apiKey = headers["apikey"]!!,
                token = headers["Authorization"]!!,
                params = mapOf("p_exhibit_id" to exhibitId)
            )

            Log.d("IMG_DEBUG", "getExhibitById: RPC response image_url=${details.image_url}")

            val exhibit = Exhibit(
                id = details.exhibit_id,
                title = details.name,
                description = details.description,
                creationYear = details.creation_year,
                hallId = details.current_hall_id,
                museumId = details.museum_id,
                authorId = details.author_id,
                authorName = details.author_name,
                museumName = details.museum_name,
                imageUrl = details.image_url,
                hallNumber = details.hall_number
            )

            // 🔥 Сохраняем в кэш для будущего использования
            DataCache.updateExhibit(exhibit)

            Result.success(exhibit)

        } catch (e: Exception) {
            Log.e("Repository", "Error loading exhibit $exhibitId", e)
            Result.failure(e)
        }
    }
    fun updateExhibitInCache(exhibit: Exhibit) {
        DataCache.updateExhibit(exhibit)
    }
    fun removeExhibitFromCache(exhibitId: Int) {
        DataCache.removeExhibit(exhibitId)
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
data class MuseumSpinnerItem(
    val id: Int,
    val name: String
) {
    override fun toString(): String = name  // Для отображения в Spinner
}