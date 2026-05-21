package com.example.museumapp.ui.exhibits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.museumapp.data.repository.AuthorRepository
import com.example.museumapp.data.repository.ExhibitRepository
import com.example.museumapp.data.repository.HallRepository
import com.example.museumapp.data.repository.MuseumRepository

class ExhibitViewModelFactory(
    private val exhibitRepository: ExhibitRepository,
    private val authorRepository: AuthorRepository,
    private val museumRepository: MuseumRepository,
    private val hallRepository: HallRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExhibitViewModel::class.java)) {
            return ExhibitViewModel(exhibitRepository, authorRepository, museumRepository, hallRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
