package com.example.museumapp.ui.exhibits

sealed class ExhibitEvent {
    data class SearchExhibits(
        val title: String,
        val authorName: String,
        val museumName: String
    ) : ExhibitEvent()

    object NavigateToAddExhibit : ExhibitEvent()
    object ClearNavigationState : ExhibitEvent()
    data class SaveExhibit(
        val title: String,
        val description: String,
        val creationYear: Int,
        val hallId: Int?,
        val authorId: Int?,
        val imageUrl: String? = null
    ) : ExhibitEvent()

    data class DeleteExhibit(val exhibitId: Int) : ExhibitEvent()
    data class UpdateExhibit(
        val exhibitId: Int,
        val title: String,
        val description: String,
        val creationYear: Int,
        val museumId: Int,
        val hallNumber: String
    ) : ExhibitEvent()

    object ResetSearch : ExhibitEvent()
    object NavigateBack : ExhibitEvent()
}