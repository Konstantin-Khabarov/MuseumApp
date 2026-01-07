package com.example.museumapp.ui.museums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.museumapp.data.repository.MuseumRepository

class MuseumViewModelFactory(
    private val museumRepository: MuseumRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MuseumViewModel::class.java)) {
            return MuseumViewModel(museumRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}