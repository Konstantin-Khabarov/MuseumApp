package com.example.museumapp.ui.halls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.museumapp.data.repository.ExhibitRepository
import com.example.museumapp.data.repository.HallRepository
import com.example.museumapp.data.repository.MuseumRepository

class HallViewModelFactory(
    private val hallRepository: HallRepository,
    private val exhibitRepository: ExhibitRepository,
    private val museumRepository: MuseumRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HallViewModel::class.java))
            return HallViewModel(hallRepository, exhibitRepository, museumRepository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
