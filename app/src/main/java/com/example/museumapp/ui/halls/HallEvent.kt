package com.example.museumapp.ui.halls

sealed class HallEvent {
    data class SearchHalls(
        val hallNumber: String,
        val name: String,
        val museumName: String
    ) : HallEvent()
    object ResetSearch : HallEvent()
    object LoadAllHalls : HallEvent()
    data class UpdateHall(
        val hallId: Int,
        val museumId: Int,
        val hallNumber: String?,
        val name: String?,
        val description: String?,
        val isStorage: Boolean
    ) : HallEvent()
    data class DeleteHall(val hallId: Int) : HallEvent()
    data class LoadHallExhibits(val hallId: Int) : HallEvent()
    data class FetchMuseumForNav(val museumId: Int) : HallEvent()
    data class SaveHall(
        val museumId: Int,
        val hallNumber: String?,
        val name: String?,
        val description: String?,
        val isStorage: Boolean
    ) : HallEvent()
}
