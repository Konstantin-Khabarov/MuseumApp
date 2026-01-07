package com.example.museumapp.ui.authors

import com.example.museumapp.data.model.Author

sealed class AuthorState {
    object Idle : AuthorState()
    data class Success(val authors: List<Author>) : AuthorState()
    data class Error(val message: String) : AuthorState()
    data class ShowMessage(val message: String) : AuthorState()
    object NavigateBack : AuthorState()
    object NavigateToAddAuthor : AuthorState()
    object NavigateToEditAuthor : AuthorState()
}