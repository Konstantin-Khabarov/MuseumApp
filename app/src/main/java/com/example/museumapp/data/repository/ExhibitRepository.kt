package com.example.museumapp.data.repository

import com.example.museumapp.data.model.Exhibit

class ExhibitRepository {

    private val mockExhibits = listOf(
        Exhibit(
            id = 1,
            title = "Мона Лиза",
            description = "Знаменитая картина Леонардо да Винчи",
            creationDate = "1503-1506",
            authorId = 1,
            museumId = 1,
            location = "Зал 1"
        ),
        Exhibit(
            id = 2,
            title = "Звездная ночь",
            description = "Картина Винсента ван Гога",
            creationDate = "1889",
            authorId = 2,
            museumId = 1,
            location = "Зал 2"
        ),
        Exhibit(
            id = 3,
            title = "Герника",
            description = "Картина Пабло Пикассо",
            creationDate = "1937",
            authorId = 3,
            museumId = 2,
            location = "Зал 3"
        ),
        Exhibit(
            id = 4,
            title = "Крик",
            description = "Картина Эдварда Мунка",
            creationDate = "1893",
            authorId = 4,
            museumId = 2,
            location = "Зал 4"
        ),
        Exhibit(
            id = 5,
            title = "Подсолнухи",
            description = "Серия картин Винсента ван Гога",
            creationDate = "1888",
            authorId = 2,
            museumId = 1,
            location = "Зал 5"
        )
    )

    suspend fun searchExhibits(
        title: String? = null,
        exhibitId: Int? = null,
        authorId: Int? = null,
        museumId: Int? = null
    ): List<Exhibit> {
        return mockExhibits.filter { exhibit ->
            (title.isNullOrEmpty() || exhibit.title.contains(title, ignoreCase = true)) &&
                    (exhibitId == null || exhibit.id == exhibitId) &&
                    (authorId == null || exhibit.authorId == authorId) &&
                    (museumId == null || exhibit.museumId == museumId)
        }
    }

    suspend fun addExhibit(exhibit: Exhibit) {
        // Логика добавления экспоната
    }

    suspend fun updateExhibit(exhibit: Exhibit) {
        // Логика обновления экспоната
    }

    suspend fun deleteExhibit(exhibitId: Int) {
        // Логика удаления экспоната
    }
}