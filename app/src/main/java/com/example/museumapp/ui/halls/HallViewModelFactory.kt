package com.example.museumapp.ui.halls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.museumapp.data.repository.HallRepository

class HallViewModelFactory(private val hallRepository: HallRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HallViewModel::class.java)) return HallViewModel(hallRepository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
