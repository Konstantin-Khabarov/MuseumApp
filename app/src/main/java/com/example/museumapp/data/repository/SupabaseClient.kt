package com.example.museumapp.data.repository

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupabaseClient {
    private const val BASE_URL = "https://bxrgvanoxllcwqvzkvny.supabase.co"
    private const val API_KEY = "sb_publishable_JCl9V3yQIob6BLqreORDhg_bFucn7zf" // anon public key из Project Settings → API
    private const val AUTH_TOKEN = "Bearer $API_KEY"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: SupabaseApi = retrofit.create(SupabaseApi::class.java)

    fun getHeaders() = mapOf(
        "apikey" to API_KEY,
        "Authorization" to AUTH_TOKEN
    )
}