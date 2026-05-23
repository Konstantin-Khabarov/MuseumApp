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
        val authorId: Int?,
        val imageUrl: String? = null
    ) : ExhibitEvent()

    data class DeleteExhibit(val exhibitId: Int) : ExhibitEvent()
    data class UpdateExhibit(
        val exhibitId: Int,
        val title: String,
        val description: String,
        val creationYear: Int,
        val hallId: Int?,
        val authorId: Int?,
        val imageUrl: String? = null
    ) : ExhibitEvent()

    object ResetSearch : ExhibitEvent()
    object NavigateBack : ExhibitEvent()
    data class FetchAuthorForNav(val authorId: Int) : ExhibitEvent()
    data class FetchMuseumForNav(val museumId: Int) : ExhibitEvent()
    data class FetchHallForNav(val hallId: Int) : ExhibitEvent()

    object LoadSpinnerData : ExhibitEvent()
    data class LoadEditFormData(val exhibitId: Int) : ExhibitEvent()
    data class LoadHallsForMuseum(val museumId: Int) : ExhibitEvent()
}
