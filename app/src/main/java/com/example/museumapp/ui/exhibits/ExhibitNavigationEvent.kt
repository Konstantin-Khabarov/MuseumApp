package com.example.museumapp.ui.exhibits

import com.example.museumapp.data.model.Author
import com.example.museumapp.data.model.Museum
import com.example.museumapp.data.repository.HallItem

sealed class ExhibitNavigationEvent {
    data class ToAuthor(val author: Author) : ExhibitNavigationEvent()
    data class ToMuseum(val museum: Museum) : ExhibitNavigationEvent()
    data class ToHall(val hall: HallItem) : ExhibitNavigationEvent()
    object ToAddExhibit : ExhibitNavigationEvent()
}
