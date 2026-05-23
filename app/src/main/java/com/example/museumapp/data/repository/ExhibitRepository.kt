package com.example.museumapp.data.repository

import com.example.museumapp.data.auth.AuthManager
import com.example.museumapp.data.cache.DataCache
import com.example.museumapp.data.model.Exhibit
import com.example.museumapp.data.model.Hall
class ExhibitRepository {
    private val api = SupabaseClient.apiService
    private val headers = SupabaseClient.getHeaders()

    companion object {
        private const val PAGE_SIZE = 20
    }

    suspend fun getExhibitsPage(page: Int, pageSize: Int = PAGE_SIZE): List<Exhibit> {
        val start = page * pageSize
        val range = "$start-${start + pageSize - 1}"

        val rpcResponse = api.searchExhibitsRpc(
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!,
            range = range,
            params = SearchParams(result_limit = pageSize, result_offset = start)
        )

        val exhibits = rpcResponse.map { it.toExhibit() }
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

            val rpcResponse = api.searchExhibitsRpc(
                apiKey = headers["apikey"]!!,
                token = headers["Authorization"]!!,
                range = range,
                params = params
            )

            val exhibits = rpcResponse.map { it.toExhibit() }

            DataCache.cacheExhibits(exhibits, clearPrevious = (page == 0))

            exhibits

        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getExhibitsByCreator(creatorId: Int): List<Exhibit> {
        val rpc = api.getExhibitsByCreatorRpc(
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!,
            params = mapOf("p_creator_id" to creatorId)
        )
        return rpc.map { it.toExhibit().copy(authorId = creatorId) }
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
            )
        } catch (e: retrofit2.HttpException) {
            throw e
        } catch (e: Exception) {
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
            museumIdFilter = "eq.$museumId"
        ).sortedBy { it.hallNumber?.toIntOrNull() ?: Int.MAX_VALUE }
    }

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

            val updated = try {
                api.updateExhibitRpc(
                    apiKey = authHeaders["apikey"]!!,
                    token = authHeaders["Authorization"]!!,
                    params = params
                )
            } catch (e: retrofit2.HttpException) {
                throw e
            }

            val updatedExhibit = updated.toExhibit()

            Result.success(updatedExhibit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteExhibit(exhibitId: Int): Result<Unit> {
        return try {
            val authHeaders = AuthManager.getApiHeaders()

            val result = api.deleteExhibitRpc(
                apiKey = authHeaders["apikey"]!!,
                token = authHeaders["Authorization"]!!,
                params = DeleteExhibitParams(p_exhibit_id = exhibitId)
            )

            if (result) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Exhibit not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getExhibitById(exhibitId: Int): Result<Exhibit> {
        return try {

            val cached = DataCache.getExhibit(exhibitId) { null }

            if (cached != null) {
                return Result.success(cached)
            }

            val details = api.getExhibitDetailsRpc(
                apiKey = headers["apikey"]!!,
                token = headers["Authorization"]!!,
                params = mapOf("p_exhibit_id" to exhibitId)
            )

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

            DataCache.updateExhibit(exhibit)

            Result.success(exhibit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    fun updateExhibitInCache(exhibit: Exhibit) {
        DataCache.updateExhibit(exhibit)
    }
    fun removeExhibitFromCache(exhibitId: Int) {
        DataCache.removeExhibit(exhibitId)
    }
}
data class AuthorSpinnerItem(
    val id: Int,
    val name: String
) {
    override fun toString(): String = name
}
data class MuseumSpinnerItem(
    val id: Int,
    val name: String
) {
    override fun toString(): String = name
}
