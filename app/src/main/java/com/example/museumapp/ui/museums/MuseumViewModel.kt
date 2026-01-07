package com.example.museumapp.ui.museums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museumapp.data.repository.MuseumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MuseumViewModel(
    private val museumRepository: MuseumRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MuseumState>(MuseumState.Idle)
    val uiState: StateFlow<MuseumState> = _uiState.asStateFlow()

    // Текущие значения поиска
    private var currentName = ""
    private var currentMuseumId = ""
    private var currentCountry = ""

    fun onEvent(event: MuseumEvent) {
        when (event) {
            is MuseumEvent.SearchMuseums -> {
                searchMuseums(event.name, event.museumId, event.country)
            }
            MuseumEvent.ResetSearch -> {
                resetSearch()
            }
            MuseumEvent.AddMuseum -> {
                _uiState.value = MuseumState.NavigateToAddMuseum
            }
            MuseumEvent.EditMuseum -> {
                _uiState.value = MuseumState.NavigateToEditMuseum
            }
            MuseumEvent.NavigateBack -> {
                _uiState.value = MuseumState.NavigateBack
            }
        }
    }

    private fun searchMuseums(name: String, museumId: String, country: String) {
        currentName = name
        currentMuseumId = museumId
        currentCountry = country

        viewModelScope.launch {
            try {
                val id = museumId.toIntOrNull()
                val museums = museumRepository.searchMuseums(
                    name = name.ifEmpty { null },
                    museumId = id,
                    country = country.ifEmpty { null }
                )

                if (museums.isEmpty()) {
                    _uiState.value = MuseumState.ShowMessage("Музеи не найдены")
                } else {
                    _uiState.value = MuseumState.Success(museums)
                }
            } catch (e: Exception) {
                _uiState.value = MuseumState.Error("Ошибка поиска: ${e.message}")
            }
        }
    }

    private fun resetSearch() {
        currentName = ""
        currentMuseumId = ""
        currentCountry = ""
        _uiState.value = MuseumState.ShowMessage("Поля поиска очищены")
        _uiState.value = MuseumState.Idle
    }

    fun getCurrentSearchValues(): Triple<String, String, String> {
        return Triple(currentName, currentMuseumId, currentCountry)
    }
}