package com.example.museumapp.ui.authors

sealed class AuthorEvent {
    data class SearchAuthors(
        val name: String
    ) : AuthorEvent()

    object ResetSearch : AuthorEvent()
    object LoadAllAuthors : AuthorEvent()
    object AddAuthor : AuthorEvent()
    object EditAuthor : AuthorEvent()
    object NavigateBack : AuthorEvent()
    data class SaveAuthor(
        val name: String,
        val biography: String?,
        val birthDate: String?,
        val deathDate: String?
    ) : AuthorEvent()
    data class DeleteAuthor(val authorId: Int) : AuthorEvent()
}