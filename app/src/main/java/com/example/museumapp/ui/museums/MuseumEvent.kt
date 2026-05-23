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
    data class SaveMuseum(
        val name: String,
        val address: String,
        val city: String,
        val country: String?,
        val website: String?
    ) : MuseumEvent()
    data class UpdateMuseum(
        val museumId: Int,
        val name: String,
        val address: String,
        val city: String,
        val country: String?,
        val website: String?
    ) : MuseumEvent()
    data class DeleteMuseum(val museumId: Int) : MuseumEvent()
    data class LoadMuseumHalls(val museumId: Int) : MuseumEvent()
}
