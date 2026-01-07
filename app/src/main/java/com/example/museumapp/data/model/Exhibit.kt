package com.example.museumapp.data.model

data class Exhibit(
    val id: Int,
    val title: String,
    val description: String,
    val creationDate: String,
    val authorId: Int? = null,
    val museumId: Int? = null,
    val location: String? = null,
    val imageUrl: String? = null
)