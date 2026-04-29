package com.example.museumapp.data.model

data class Hall(
    val hall_id: Int,
    val museum_id: Int,
    val hall_number: String?,
    val name: String?,
    val description: String?,
    val is_storage: Boolean?
)