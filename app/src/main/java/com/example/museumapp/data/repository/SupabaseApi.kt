package com.example.museumapp.data.repository

import com.example.museumapp.data.model.Author
import com.example.museumapp.data.model.Exhibit
import com.example.museumapp.data.model.Hall
import com.example.museumapp.data.model.Museum
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SupabaseApi {

    // CREATORS
    @GET("rest/v1/creator?select=*")
    suspend fun getAllCreators(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String
    ): List<Author>

    @POST("rest/v1/creator")
    suspend fun insertCreator(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body creator: Author
    ): List<Author>

    @PATCH("rest/v1/creator?id=eq.{id}")
    suspend fun updateCreator(
        @Path("id") id: Int,
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Body creator: Author
    ): List<Author>

    @DELETE("rest/v1/creator?id=eq.{id}")
    suspend fun deleteCreator(
        @Path("id") id: Int,
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String
    ): Response<Unit>

    // EXHIBITS
    @GET("rest/v1/exhibit")
    suspend fun getAllExhibits(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Header("Range") range: String = "0-19"
    ): List<Exhibit>

    @POST("rest/v1/rpc/search_exhibits")
    suspend fun searchExhibitsRpc(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Header("Range") range: String = "0-19",
        @Body params: SearchParams
    ): List<ExhibitRpcResponse>

    @POST("rest/v1/rpc/get_exhibit_details")
    suspend fun getExhibitDetailsRpc(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Body params: Map<String, Int>
    ): ExhibitDetailResponse // Supabase RPC возвращает массив даже для 1 строки

    @POST("rest/v1/exhibit")
    suspend fun insertExhibit(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body exhibit: ExhibitInsertRequest
    ): List<ExhibitRpcResponse>

    @POST("rest/v1/exhibit_creator")
    suspend fun insertExhibitCreator(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body relation: ExhibitCreatorRequest
    ): List<ExhibitCreatorResponse>

    @POST("rest/v1/rpc/update_exhibit")
    suspend fun updateExhibitRpc(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body params: UpdateExhibitParams
    ): ExhibitRpcResponse

    @POST("rest/v1/rpc/delete_exhibit_with_relations")
    suspend fun deleteExhibitRpc(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body params: DeleteExhibitParams
    ): Boolean

    // MUSEUMS
    @GET("rest/v1/museum?select=*")
    suspend fun getAllMuseums(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String
    ): List<Museum>

    // ==================== HALLS ====================

    // Получение всех залов (для справочников)
    @GET("rest/v1/hall?select=*")
    suspend fun getAllHalls(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String
    ): List<Hall>

    // 🔥 НОВЫЙ: Получение залов по музею (фильтрация на сервере)
    @GET("rest/v1/hall")
    suspend fun getHallsByMuseumId(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Query("museum_id") museumIdFilter: String  // ← Обратите внимание: просто "museum_id"
    ): List<Hall>
}

data class SearchParams(
    val title_filter: String? = null,
    val author_name_filter: String? = null,
    val museum_name_filter: String? = null,
    val result_limit: Int = 20,
    val result_offset: Int = 0
)

data class ExhibitRpcResponse(
    val exhibit_id: Int,
    val name: String,
    val description: String,
    val creation_year: Int,
    val current_hall_id: Int?,
    val creator_ids: List<Int>?,
    val museum_id: Int?,
    val author_name: String?,
    val museum_name: String?
)

data class ExhibitInsertRequest(
    val name: String,
    val description: String,
    val creation_year: Int,
    val current_hall_id: Int? = null
    // imageUrl можно добавить при необходимости
)

data class ExhibitCreatorRequest(
    val exhibit_id: Int,
    val creator_id: Int
)

data class ExhibitDetailResponse(
    val exhibit_id: Int,
    val name: String,
    val description: String,
    val creation_year: Int,
    val current_hall_id: Int?,
    val museum_id: Int?,
    val museum_name: String?,
    val author_name: String?
)

data class ExhibitCreatorResponse(
    val exhibit_id: Int,
    val creator_id: Int
)
data class DeleteExhibitParams(
    val p_exhibit_id: Int
)
data class UpdateExhibitParams(
    val p_exhibit_id: Int,
    val p_name: String,
    val p_description: String,
    val p_creation_year: Int,
    val p_museum_id: Int,
    val p_hall_number: String
)