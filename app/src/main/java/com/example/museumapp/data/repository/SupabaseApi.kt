package com.example.museumapp.data.repository

import com.example.museumapp.data.model.Author
import com.example.museumapp.data.model.Exhibit
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
        @Header("Range") range: String = "0-19", // Пагинация: 0-49, 50-99 и т.д.
        @Query("select") select: String = "*"
    ): List<Exhibit>

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

    @GET("rest/v1/exhibit")
    suspend fun searchExhibits(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Header("Range") range: String = "0-19",
        @Query("name.ilike") nameFilter: String? = null,
        @Query("creation_year.gte") yearFrom: Int? = null,
        @Query("creation_year.lte") yearTo: Int? = null
    ): List<Exhibit>
    @POST("rest/v1/exhibit")
    suspend fun insertExhibit(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body exhibit: Exhibit
    ): List<Exhibit>

    @PATCH("rest/v1/exhibit?id=eq.{id}")
    suspend fun updateExhibit(
        @Path("id") id: Int,
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Body exhibit: Exhibit
    ): List<Exhibit>

    @DELETE("rest/v1/exhibit?id=eq.{id}")
    suspend fun deleteExhibit(
        @Path("id") id: Int,
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String
    ): Response<Unit>

    // MUSEUMS
    @GET("rest/v1/museum?select=*")
    suspend fun getAllMuseums(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String
    ): List<Museum>
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
    val museum_name: String?
)