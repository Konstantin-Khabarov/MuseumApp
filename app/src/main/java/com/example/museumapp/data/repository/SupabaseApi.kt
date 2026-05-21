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

    @PATCH("rest/v1/creator")
    suspend fun updateCreator(
        @Query("creator_id") creatorIdFilter: String,
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body creator: AuthorUpdateRequest
    ): List<Author>

    @POST("rest/v1/rpc/update_creator")
    suspend fun updateCreatorRpc(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Body params: UpdateCreatorParams
    ): Author

    @POST("rest/v1/rpc/delete_creator_with_relations")
    suspend fun deleteCreatorRpc(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Body params: DeleteCreatorParams
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

    @POST("rest/v1/rpc/add_exhibit")
    suspend fun addExhibitRpc(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Body params: AddExhibitParams
    ): Int

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
    @POST("rest/v1/rpc/update_hall")
    suspend fun updateHallRpc(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Body params: UpdateHallParams
    ): Response<Unit>

    @POST("rest/v1/rpc/delete_hall_with_relations")
    suspend fun deleteHallRpc(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Body params: DeleteHallParams
    ): Response<Unit>

    @POST("rest/v1/rpc/add_hall")
    suspend fun addHallRpc(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Body params: AddHallParams
    ): Int

    @POST("rest/v1/rpc/add_museum")
    suspend fun addMuseumRpc(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Body params: AddMuseumParams
    ): Int

    @POST("rest/v1/rpc/update_museum")
    suspend fun updateMuseumRpc(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Body params: UpdateMuseumParams
    ): Response<Unit>

    @POST("rest/v1/rpc/delete_museum_with_relations")
    suspend fun deleteMuseumRpc(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Body params: DeleteMuseumParams
    ): Response<Unit>

    @GET("rest/v1/museum?select=*")
    suspend fun getAllMuseums(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String
    ): List<Museum>

    // ==================== HALLS ====================

    @GET("rest/v1/hall?select=*")
    suspend fun getAllHalls(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String
    ): List<Hall>

    @GET("rest/v1/hall?select=hall_id,museum_id,hall_number,name,description,is_storage,museum(name)")
    suspend fun getAllHallsWithMuseum(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String
    ): List<HallWithMuseumResponse>

    @GET("rest/v1/hall")
    suspend fun getHallsByMuseumId(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Query("museum_id") museumIdFilter: String
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
    val museum_name: String?,
    val image_url: String? = null
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
    val author_name: String?,
    val image_url: String? = null
)

data class ExhibitCreatorResponse(
    val exhibit_id: Int,
    val creator_id: Int
)
data class AuthorUpdateRequest(
    val name: String,
    val biography: String?,
    val birth_date: String?,
    val death_date: String?,
    val photo_url: String?
)

data class UpdateCreatorParams(
    val p_creator_id: Int,
    val p_name: String,
    val p_biography: String?,
    val p_birth_date: String?,
    val p_death_date: String?,
    val p_photo_url: String?
)

data class DeleteExhibitParams(
    val p_exhibit_id: Int
)

data class DeleteCreatorParams(
    val p_creator_id: Int
)

data class HallWithMuseumResponse(
    val hall_id: Int,
    val museum_id: Int,
    val hall_number: String?,
    val name: String?,
    val description: String?,
    val is_storage: Boolean?,
    val museum: MuseumNameResponse?
)

data class MuseumNameResponse(val name: String?)

data class UpdateHallParams(
    val p_hall_id: Int,
    val p_museum_id: Int,
    val p_hall_number: String?,
    val p_name: String?,
    val p_description: String?,
    val p_is_storage: Boolean
)

data class DeleteHallParams(val p_hall_id: Int)

data class AddHallParams(
    val p_museum_id: Int,
    val p_hall_number: String?,
    val p_name: String?,
    val p_description: String?,
    val p_is_storage: Boolean
)

data class AddMuseumParams(
    val p_name: String,
    val p_address: String,
    val p_city: String,
    val p_country: String?,
    val p_website: String?
)

data class UpdateMuseumParams(
    val p_museum_id: Int,
    val p_name: String,
    val p_address: String,
    val p_city: String,
    val p_country: String?,
    val p_website: String?
)

data class DeleteMuseumParams(
    val p_museum_id: Int
)

data class AddExhibitParams(
    val p_name: String,
    val p_description: String,
    val p_creation_year: Int,
    val p_hall_id: Int?,
    val p_author_id: Int?,
    val p_image_url: String?
)
data class UpdateExhibitParams(
    val p_exhibit_id: Int,
    val p_name: String,
    val p_description: String,
    val p_creation_year: Int,
    val p_museum_id: Int,
    val p_hall_number: String
)