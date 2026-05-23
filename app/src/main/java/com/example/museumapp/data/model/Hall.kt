package com.example.museumapp.data.model

import com.google.gson.annotations.SerializedName

data class Hall(
    @SerializedName("hall_id") val hallId: Int,
    @SerializedName("museum_id") val museumId: Int,
    @SerializedName("hall_number") val hallNumber: String?,
    val name: String?,
    val description: String?,
    @SerializedName("is_storage") val isStorage: Boolean?
) {
    override fun toString(): String {
        val number = hallNumber?.let { "№$it" } ?: ""
        val nm = name?.takeIf { it.isNotBlank() } ?: ""
        return listOf(number, nm).filter { it.isNotEmpty() }.joinToString(" ")
    }
}
