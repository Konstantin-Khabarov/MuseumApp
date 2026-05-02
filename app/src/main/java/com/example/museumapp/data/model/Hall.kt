package com.example.museumapp.data.model

data class Hall(
    val hall_id: Int,
    val museum_id: Int,
    val hall_number: String?,
    val name: String?,
    val description: String?,
    val is_storage: Boolean?
) {
    // 🔥 Удобное отображение в Spinner: "Зал №5 — Основная экспозиция"
    override fun toString(): String {
        val number = hall_number?.let { "№$it" } ?: ""
        val name = name?.takeIf { it.isNotBlank() } ?: ""
        return listOf(number, name).filter { it.isNotEmpty() }.joinToString(" ")
    }
}