package com.example.museumapp.ui.main

sealed class MainState {
    object Idle : MainState()
    object Loading : MainState()
    data class ShowMessage(val message: String) : MainState()
    data class NavigateTo(val destination: NavigationDestination) : MainState()
}

sealed class NavigationDestination(val requiresAuth: Boolean = false) {
    object Login : NavigationDestination()  // 🔥 Было Registration

    object ExhibitManagement : NavigationDestination()  // 🔥 Требует авторизации
    object AuthorManagement : NavigationDestination()
    object MuseumManagement : NavigationDestination()
    object HallManagement : NavigationDestination()
}