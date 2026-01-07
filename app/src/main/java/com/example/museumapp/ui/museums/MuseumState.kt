package com.example.museumapp.ui.museums

import com.example.museumapp.data.model.Museum

sealed class MuseumState {
    object Idle : MuseumState()
    data class Success(val museums: List<Museum>) : MuseumState()
    data class Error(val message: String) : MuseumState()
    data class ShowMessage(val message: String) : MuseumState()
    object NavigateBack : MuseumState()
    object NavigateToAddMuseum : MuseumState()
    object NavigateToEditMuseum : MuseumState()
}