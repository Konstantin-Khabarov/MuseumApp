package com.example.museumapp.ui.exhibits

sealed class ExhibitEvent {
    data class SearchExhibits(
        val title: String,
        val authorName: String,
        val museumName: String
    ) : ExhibitEvent()

    object NavigateToAddExhibit : ExhibitEvent()
    data class SaveExhibit(
        val title: String,
        val description: String,
        val creationYear: Int,
        val hallId: Int?,
        val authorId: Int?
    ) : ExhibitEvent()

    object ResetSearch : ExhibitEvent()
    object NavigateBack : ExhibitEvent()
}