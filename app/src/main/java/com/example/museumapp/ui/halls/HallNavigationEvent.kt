package com.example.museumapp.ui.halls

import com.example.museumapp.data.model.Museum

sealed class HallNavigationEvent {
    data class ToMuseum(val museum: Museum) : HallNavigationEvent()
}
