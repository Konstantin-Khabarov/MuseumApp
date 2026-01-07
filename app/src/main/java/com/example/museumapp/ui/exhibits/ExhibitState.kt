package com.example.museumapp.ui.exhibits

import com.example.museumapp.data.model.Exhibit

sealed class ExhibitState {
    object Idle : ExhibitState()
    data class Success(val exhibits: List<Exhibit>) : ExhibitState()
    data class Error(val message: String) : ExhibitState()
    data class ShowMessage(val message: String) : ExhibitState()
    object NavigateBack : ExhibitState()
    object NavigateToAddExhibit : ExhibitState()
}