package com.example.museumapp.data.model

import com.google.gson.annotations.SerializedName

data class Museum(
    @SerializedName("museum_id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("city")
    val city: String,
    @SerializedName("country")
    val country: String? = null,
    @SerializedName("website")
    val website: String? = null
)
