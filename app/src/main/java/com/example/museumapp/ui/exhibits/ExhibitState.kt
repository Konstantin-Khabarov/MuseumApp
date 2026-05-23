package com.example.museumapp.ui.exhibits

import com.example.museumapp.data.model.Exhibit
import com.example.museumapp.data.model.Hall
import com.example.museumapp.data.repository.AuthorSpinnerItem
import com.example.museumapp.data.repository.MuseumSpinnerItem

sealed class ExhibitState {
    object Idle : ExhibitState()
    object Loading : ExhibitState()
    data class Success(val exhibits: List<Exhibit>) : ExhibitState()
    data class Error(val message: String) : ExhibitState()
    data class ShowMessage(val message: String) : ExhibitState()
    object NavigateBack : ExhibitState()
    object NavigateToAddExhibit : ExhibitState()
    data class ExhibitDetailsLoaded(val exhibit: Exhibit) : ExhibitState()

    data class SpinnerDataLoaded(
        val authors: List<AuthorSpinnerItem>,
        val museums: List<MuseumSpinnerItem>
    ) : ExhibitState()

    data class EditFormLoaded(
        val exhibit: Exhibit,
        val authors: List<AuthorSpinnerItem>,
        val museums: List<MuseumSpinnerItem>,
        val halls: List<Hall>
    ) : ExhibitState()

    data class HallsLoaded(val halls: List<Hall>) : ExhibitState()
}
