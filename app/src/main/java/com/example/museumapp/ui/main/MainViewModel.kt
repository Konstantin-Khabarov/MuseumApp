package com.example.museumapp.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<MainState>(MainState.Idle)
    val uiState: StateFlow<MainState> = _uiState.asStateFlow()

    fun onLoginClicked() {
        viewModelScope.launch {
            _uiState.value = MainState.NavigateTo(NavigationDestination.Login)
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

    fun onHallsInfoClicked() {
        viewModelScope.launch {
            _uiState.value = MainState.NavigateTo(NavigationDestination.HallManagement)
        }
    }

    fun resetState() {
        viewModelScope.launch {
            _uiState.value = MainState.Idle
        }
    }
}
