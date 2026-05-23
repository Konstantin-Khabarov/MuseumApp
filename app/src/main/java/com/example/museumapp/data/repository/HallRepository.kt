package com.example.museumapp.data.repository

import com.example.museumapp.data.auth.AuthManager
import com.example.museumapp.data.cache.DataCache
import com.example.museumapp.data.model.Exhibit

data class HallItem(
    val hallId: Int,
    val museumId: Int,
    val hallNumber: String?,
    val name: String?,
    val museumName: String?,
    val description: String? = null,
    val isStorage: Boolean? = null
)

class HallRepository {
    private val api = SupabaseClient.apiService

    suspend fun getAllHalls(): List<HallItem> {
        val headers = SupabaseClient.getHeaders()
        return api.getAllHallsWithMuseum(
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!
        ).map { it.toHallItem() }
    }

    suspend fun searchHalls(
        hallNumber: String? = null,
        name: String? = null,
        museumName: String? = null
    ): List<HallItem> {
        return getAllHalls().filter { hall ->
            (hallNumber.isNullOrEmpty() || hall.hallNumber?.contains(hallNumber, ignoreCase = true) == true) &&
            (name.isNullOrEmpty() || hall.name?.contains(name, ignoreCase = true) == true) &&
            (museumName.isNullOrEmpty() || hall.museumName?.contains(museumName, ignoreCase = true) == true)
        }
    }

    suspend fun getHallsByMuseum(museumId: Int): List<HallItem> {
        return try { getAllHalls().filter { it.museumId == museumId } }
        catch (e: Exception) { emptyList() }
    }

    suspend fun getExhibitsByHall(hallId: Int): List<Exhibit> {
        val headers = SupabaseClient.getHeaders()
        return api.getExhibitsByHallRpc(
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!,
            params = mapOf("p_hall_id" to hallId)
        ).map { it.toExhibit() }
    }

    suspend fun getHallById(id: Int): HallItem? {
        return try { getAllHalls().firstOrNull { it.hallId == id } }
        catch (e: Exception) { null }
    }

    suspend fun getMuseumsForSpinner(): List<MuseumSpinnerItem> {
        return DataCache.getMuseums {
            val headers = SupabaseClient.getHeaders()
            api.getAllMuseums(headers["apikey"]!!, headers["Authorization"]!!)
                .map { MuseumSpinnerItem(id = it.id, name = it.name) }
        }
    }

    suspend fun addHall(
        museumId: Int,
        hallNumber: String?,
        name: String?,
        description: String?,
        isStorage: Boolean
    ): Result<Unit> {
        return try {
            val headers = AuthManager.getApiHeaders()
            api.addHallRpc(
                apiKey = headers["apikey"]!!,
                token = headers["Authorization"]!!,
                params = AddHallParams(
                    p_museum_id = museumId,
                    p_hall_number = hallNumber,
                    p_name = name,
                    p_description = description,
                    p_is_storage = isStorage
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateHall(
        hallId: Int, museumId: Int, hallNumber: String?,
        name: String?, description: String?, isStorage: Boolean
    ): Result<Unit> {
        return try {
            val headers = AuthManager.getApiHeaders()
            val response = api.updateHallRpc(
                apiKey = headers["apikey"]!!,
                token = headers["Authorization"]!!,
                params = UpdateHallParams(
                    p_hall_id = hallId, p_museum_id = museumId,
                    p_hall_number = hallNumber, p_name = name,
                    p_description = description, p_is_storage = isStorage
                )
            )
            if (!response.isSuccessful) Result.failure(Exception("HTTP ${response.code()}: ${response.errorBody()?.string()}"))
            else Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteHall(hallId: Int): Result<Unit> {
        return try {
            val headers = AuthManager.getApiHeaders()
            val response = api.deleteHallRpc(
                apiKey = headers["apikey"]!!,
                token = headers["Authorization"]!!,
                params = DeleteHallParams(p_hall_id = hallId)
            )
            if (!response.isSuccessful) Result.failure(Exception("HTTP ${response.code()}: ${response.errorBody()?.string()}"))
            else Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun HallWithMuseumResponse.toHallItem() = HallItem(
        hallId = hallId,
        museumId = museumId,
        hallNumber = hallNumber,
        name = name,
        museumName = museum?.name,
        description = description,
        isStorage = isStorage
    )
}
