package com.example.museumapp.data.repository

import com.example.museumapp.data.model.Museum

class MuseumRepository {

    private val mockMuseums = listOf(
        Museum(
            id = 1,
            name = "Лувр",
            address = "Rue de Rivoli, 75001 Paris, France",
            country = "Франция"
        ),
        Museum(
            id = 2,
            name = "Эрмитаж",
            address = "Дворцовая площадь, 2, Санкт-Петербург, Россия",
            country = "Россия"
        ),
        Museum(
            id = 3,
            name = "Британский музей",
            address = "Great Russell St, London WC1B 3DG, UK",
            country = "Великобритания"
        ),
        Museum(
            id = 4,
            name = "Метрополитен-музей",
            address = "1000 5th Ave, New York, NY 10028, USA",
            country = "США"
        ),
        Museum(
            id = 5,
            name = "Прадо",
            address = "Calle de Ruiz de Alarcón, 23, 28014 Madrid, Spain",
            country = "Испания"
        )
    )

    suspend fun searchMuseums(
        name: String? = null,
        museumId: Int? = null,
        country: String? = null
    ): List<Museum> {
        return mockMuseums.filter { museum ->
            (name.isNullOrEmpty() || museum.name.contains(name, ignoreCase = true)) &&
                    (museumId == null || museum.id == museumId) &&
                    (country.isNullOrEmpty() || museum.country.contains(country, ignoreCase = true))
        }
    }

    suspend fun addMuseum(museum: Museum) {
        // Логика добавления музея
    }

    suspend fun updateMuseum(museum: Museum) {
        // Логика обновления музея
    }

    suspend fun deleteMuseum(museumId: Int) {
        // Логика удаления музея
    }
}