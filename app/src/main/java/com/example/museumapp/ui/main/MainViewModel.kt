package com.example.museumapp.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    // Состояние UI
    private val _uiState = MutableStateFlow<MainState>(MainState.Idle)
    val uiState: StateFlow<MainState> = _uiState.asStateFlow()

    // Обработка нажатий кнопок
    fun onRegistrationClicked() {
        viewModelScope.launch {
            _uiState.value = MainState.NavigateTo(NavigationDestination.Registration)
        }
    }

    fun onExhibitSearchClicked() {
        viewModelScope.launch {
            _uiState.value = MainState.NavigateTo(NavigationDestination.ExhibitManagement)
        }
    }

    fun onAuthorSearchClicked() {
        viewModelScope.launch {
            _uiState.value = MainState.NavigateTo(NavigationDestination.AuthorManagement)
        }
    }

    fun onMuseumInfoClicked() {
        viewModelScope.launch {
            _uiState.value = MainState.NavigateTo(NavigationDestination.MuseumManagement)
        }
    }

    fun onExhibitionsInfoClicked() {
        viewModelScope.launch {
            _uiState.value = MainState.NavigateTo(NavigationDestination.ExhibitionManagement)
        }
    }

    // Сброс состояния после обработки
    fun resetState() {
        viewModelScope.launch {
            _uiState.value = MainState.Idle
        }
    }
}