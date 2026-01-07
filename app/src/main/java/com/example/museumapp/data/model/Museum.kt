package com.example.museumapp.data.model

data class Museum(
    val id: Int,
    val name: String,
    val address: String,
    val country: String,
    val website: String? = null
)