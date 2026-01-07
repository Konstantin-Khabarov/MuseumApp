package com.example.museumapp.ui.exhibits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.museumapp.data.repository.ExhibitRepository

class ExhibitViewModelFactory(
    private val exhibitRepository: ExhibitRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExhibitViewModel::class.java)) {
            return ExhibitViewModel(exhibitRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}