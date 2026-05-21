package com.example.museumapp.ui.authors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.museumapp.data.repository.AuthorRepository
import com.example.museumapp.data.repository.ExhibitRepository

class AuthorViewModelFactory(
    private val authorRepository: AuthorRepository,
    private val exhibitRepository: ExhibitRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthorViewModel::class.java)) {
            return AuthorViewModel(authorRepository, exhibitRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
