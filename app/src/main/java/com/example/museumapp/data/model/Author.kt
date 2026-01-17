package com.example.museumapp.data.model

import com.google.gson.annotations.SerializedName

data class Author(
    @SerializedName("creator_id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("biography")
    val biography: String?,
    @SerializedName("birth_date")
    val birthDate: String?,
    @SerializedName("death_date")
    val deathDate: String?
)
