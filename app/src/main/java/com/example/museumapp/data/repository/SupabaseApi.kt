package com.example.museumapp.data.repository

import com.example.museumapp.data.model.Author
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface SupabaseApi {
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
}