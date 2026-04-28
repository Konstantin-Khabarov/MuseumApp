package com.example.museumapp.data.model

import com.google.gson.annotations.SerializedName

data class Exhibit(
    @SerializedName("exhibit_id")
    val id: Int,
    @SerializedName("name")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("creation_year")
    val creationDate: String,
    val authorId: Int? = null,
    val museumId: Int? = null,
    val imageUrl: String? = null
)