package com.example.museumapp.ui.museums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museumapp.data.repository.MuseumRepository
import com.example.museumapp.ui.authors.AuthorState
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
    private var currentCity = ""

    init {
        loadAllMuseums()
    }

    fun onEvent(event: MuseumEvent) {
        when (event) {
            is MuseumEvent.SearchMuseums -> {
                searchMuseums(event.name, event.city)
            }
            MuseumEvent.ResetSearch -> {
                resetSearch()
            }
            MuseumEvent.LoadAllMuseums -> {
                loadAllMuseums()
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

    private fun loadAllMuseums() {
        viewModelScope.launch {
            try {
                val museums = museumRepository.getAllMuseums()
                _uiState.value = MuseumState.Success(museums)
            } catch (e: Exception) {
                _uiState.value = MuseumState.Error("Ошибка загрузки: ${e.message}")
            }
        }
    }

    private fun searchMuseums(name: String, city: String) {
        currentName = name
        currentCity = city

        viewModelScope.launch {
            try {
                val museums = museumRepository.searchMuseums(
                    name = name.ifEmpty { null },
                    city = city.ifEmpty { null }
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
        currentCity = ""
        _uiState.value = MuseumState.ShowMessage("Поля поиска очищены")
        _uiState.value = MuseumState.Idle
    }

    fun getCurrentSearchValues(): Pair<String, String> {
        return Pair(currentName, currentCity)
    }
}