package com.example.museumapp.data.model

data class Author (
    val id: Int,
    val name: String,
    val biography: String? = null,
    val birthDate: String? = null,
    val deathDate: String? = null
)
