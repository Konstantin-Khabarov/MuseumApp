package com.example.museumapp.ui.exhibits

sealed class ExhibitEvent {
    data class SearchExhibits(
        val title: String,
        val exhibitId: String
    ) : ExhibitEvent()

    object ResetSearch : ExhibitEvent()
    object AddExhibit : ExhibitEvent()
    object NavigateBack : ExhibitEvent()
}