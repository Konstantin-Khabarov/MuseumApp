package com.example.museumapp.ui.museums

sealed class MuseumEvent {
    data class SearchMuseums(
        val name: String,
        val city: String
    ) : MuseumEvent()

    object ResetSearch : MuseumEvent()
    object LoadAllMuseums : MuseumEvent()
    object AddMuseum : MuseumEvent()
    object EditMuseum : MuseumEvent()
    object NavigateBack : MuseumEvent()
}