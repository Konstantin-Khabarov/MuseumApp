package com.example.museumapp.data.repository

import com.example.museumapp.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupabaseClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("${BuildConfig.SUPABASE_URL}/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: SupabaseApi = retrofit.create(SupabaseApi::class.java)

    fun getHeaders() = mapOf(
        "apikey" to BuildConfig.SUPABASE_ANON_KEY,
        "Authorization" to "Bearer ${BuildConfig.SUPABASE_ANON_KEY}",
        "Content-Type" to "application/json",
        "Prefer" to "return=representation"
    )
}
