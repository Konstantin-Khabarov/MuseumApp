package com.example.museumapp.data.repository

import com.example.museumapp.data.model.Exhibit
import com.example.museumapp.data.model.Author
import com.example.museumapp.data.model.Museum

class ExhibitRepository {
    private val api = SupabaseClient.apiService

    // Получение всех экспонатов
    suspend fun getAllExhibits(): List<Exhibit> {
        val headers = SupabaseClient.getHeaders()
        val response = api.getAllExhibits(
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!
        )
        println("DEBUG: Supabase response = $response")
        return response
    }

    // Поиск экспонатов по различным критериям
    suspend fun searchExhibits(
        title: String? = null,
        authorName: String? = null,
        museumName: String? = null
    ): List<Exhibit> {
        val allExhibits = getAllExhibits()

        // Если нет критериев поиска, возвращаем все экспонаты
        if (title.isNullOrEmpty() && authorName.isNullOrEmpty() && museumName.isNullOrEmpty()) {
            return allExhibits
        }

        // Получаем всех авторов и музеи для поиска по именам
        val authors = getAllAuthors()
        val museums = getAllMuseums()

        return allExhibits.filter { exhibit ->
            var matches = true

            // Поиск по названию экспоната
            if (!title.isNullOrEmpty()) {
                matches = matches && exhibit.title.contains(title, ignoreCase = true)
            }

            // Поиск по имени автора
            if (!authorName.isNullOrEmpty()) {
                val author = authors.find { it.id == exhibit.authorId }
                val authorMatches = author?.name?.contains(authorName, ignoreCase = true) ?: false
                matches = matches && authorMatches
            }

            // Поиск по названию музея
            if (!museumName.isNullOrEmpty()) {
                val museum = museums.find { it.id == exhibit.museumId }
                val museumMatches = museum?.name?.contains(museumName, ignoreCase = true) ?: false
                matches = matches && museumMatches
            }

            matches
        }
    }

    // Поиск экспонатов по ID автора
    suspend fun getExhibitsByAuthorId(authorId: Int): List<Exhibit> {
        val allExhibits = getAllExhibits()
        return allExhibits.filter { it.authorId == authorId }
    }

    // Поиск экспонатов по ID музея
    suspend fun getExhibitsByMuseumId(museumId: Int): List<Exhibit> {
        val allExhibits = getAllExhibits()
        return allExhibits.filter { it.museumId == museumId }
    }

    // Добавление нового экспоната
    suspend fun insertExhibit(exhibit: Exhibit): List<Exhibit> {
        val headers = SupabaseClient.getHeaders()
        return api.insertExhibit(
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!,
            exhibit = exhibit
        )
    }

    // Обновление экспоната
    suspend fun updateExhibit(id: Int, exhibit: Exhibit): List<Exhibit> {
        val headers = SupabaseClient.getHeaders()
        return api.updateExhibit(
            id = id,
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!,
            exhibit = exhibit
        )
    }

    // Удаление экспоната
    suspend fun deleteExhibit(id: Int) {
        val headers = SupabaseClient.getHeaders()
        api.deleteExhibit(
            id = id,
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!
        )
    }

    // Вспомогательные методы для получения авторов и музеев
    private suspend fun getAllAuthors(): List<Author> {
        val headers = SupabaseClient.getHeaders()
        val response = api.getAllCreators(
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!
        )
        return response
    }

    private suspend fun getAllMuseums(): List<Museum> {
        val headers = SupabaseClient.getHeaders()
        val response = api.getAllMuseums(
            apiKey = headers["apikey"]!!,
            token = headers["Authorization"]!!
        )
        return response
    }
}