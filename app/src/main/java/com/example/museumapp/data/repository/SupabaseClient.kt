package com.example.museumapp.data.repository

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

object SupabaseClient {
    private const val BASE_URL = "https://bxrgvanoxllcwqvzkvny.supabase.co"
    private const val API_KEY = "sb_publishable_JCl9V3yQIob6BLqreORDhg_bFucn7zf" // anon public key из Project Settings → API
    private const val AUTH_TOKEN = "Bearer $API_KEY"

    // Увеличиваем таймауты для работы с большим объемом данных
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: SupabaseApi = retrofit.create(SupabaseApi::class.java)

    fun getHeaders() = mapOf(
        "apikey" to API_KEY,
        "Authorization" to AUTH_TOKEN
    )
}