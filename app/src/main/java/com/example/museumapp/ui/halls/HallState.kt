package com.example.museumapp.ui.halls

import com.example.museumapp.data.model.Exhibit
import com.example.museumapp.data.repository.HallItem

sealed class HallState {
    object Idle : HallState()
    object Loading : HallState()
    data class Success(val halls: List<HallItem>) : HallState()
    data class Error(val message: String) : HallState()
    data class ShowMessage(val message: String) : HallState()
    object HallAdded : HallState()
    object HallUpdated : HallState()
    object HallDeleted : HallState()
    object ExhibitsLoading : HallState()
    data class HallExhibitsLoaded(val exhibits: List<Exhibit>) : HallState()
}
