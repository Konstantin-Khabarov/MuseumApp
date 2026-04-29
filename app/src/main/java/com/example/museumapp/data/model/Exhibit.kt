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
    val creationYear: Int,
    @SerializedName("current_hall_id")
    val hallId: Int? = null,
    val authorId: Int? = null, // Для удобства (первый автор)
    val museumId: Int? = null, // Для удобства (через hall)
    val authorName: String? = null,
    val museumName: String? = null,

    val imageUrl: String? = null
)