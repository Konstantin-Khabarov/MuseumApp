package com.example.museumapp.ui.authors

import com.example.museumapp.data.model.Author
import com.example.museumapp.data.model.Exhibit

sealed class AuthorState {
    object Idle : AuthorState()
    object Loading : AuthorState()
    data class Success(val authors: List<Author>) : AuthorState()
    data class Error(val message: String) : AuthorState()
    data class ShowMessage(val message: String) : AuthorState()
    object NavigateBack : AuthorState()
    object NavigateToAddAuthor : AuthorState()
    object NavigateToEditAuthor : AuthorState()
    object AuthorAdded : AuthorState()
    object AuthorUpdated : AuthorState()
    object AuthorDeleted : AuthorState()
    object ExhibitsLoading : AuthorState()
    data class AuthorExhibitsLoaded(val exhibits: List<Exhibit>) : AuthorState()
}