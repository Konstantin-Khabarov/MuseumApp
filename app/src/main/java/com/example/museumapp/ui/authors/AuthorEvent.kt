package com.example.museumapp.ui.authors

sealed class AuthorEvent {
    data class SearchAuthors(
        val name: String,
        val authorId: String
    ) : AuthorEvent()

    object ResetSearch : AuthorEvent()
    object AddAuthor : AuthorEvent()
    object EditAuthor : AuthorEvent()
    object NavigateBack : AuthorEvent()
}