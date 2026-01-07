package com.example.museumapp.ui.museums

sealed class MuseumEvent {
    data class SearchMuseums(
        val name: String,
        val museumId: String,
        val country: String
    ) : MuseumEvent()

    object ResetSearch : MuseumEvent()
    object AddMuseum : MuseumEvent()
    object EditMuseum : MuseumEvent()
    object NavigateBack : MuseumEvent()
}