package com.example.museumapp.ui.museums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museumapp.data.repository.HallRepository
import com.example.museumapp.data.repository.MuseumRepository
import com.example.museumapp.util.parseError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MuseumViewModel(
    private val museumRepository: MuseumRepository,
    private val hallRepository: HallRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MuseumState>(MuseumState.Idle)
    val uiState: StateFlow<MuseumState> = _uiState.asStateFlow()

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
            is MuseumEvent.SaveMuseum -> saveMuseum(event)
            is MuseumEvent.UpdateMuseum -> updateMuseum(event)
            is MuseumEvent.DeleteMuseum -> deleteMuseum(event.museumId)
            is MuseumEvent.LoadMuseumHalls -> loadMuseumHalls(event.museumId)
        }
    }

    private fun loadMuseumHalls(museumId: Int) {
        viewModelScope.launch {
            _uiState.value = MuseumState.HallsLoading
            try {
                val halls = hallRepository.getHallsByMuseum(museumId)
                _uiState.value = MuseumState.MuseumHallsLoaded(halls)
            } catch (e: Exception) {
                _uiState.value = MuseumState.Error(parseError(e))
            }
        }
    }

    private fun saveMuseum(event: MuseumEvent.SaveMuseum) {
        viewModelScope.launch {
            _uiState.value = MuseumState.Loading
            museumRepository.addMuseum(
                name = event.name,
                address = event.address,
                city = event.city,
                country = event.country,
                website = event.website
            ).onSuccess {
                _uiState.value = MuseumState.MuseumAdded
            }.onFailure { e ->
                _uiState.value = MuseumState.Error(parseError(e))
            }
        }
    }

    private fun updateMuseum(event: MuseumEvent.UpdateMuseum) {
        viewModelScope.launch {
            _uiState.value = MuseumState.Loading
            museumRepository.updateMuseum(
                museumId = event.museumId,
                name = event.name,
                address = event.address,
                city = event.city,
                country = event.country,
                website = event.website
            ).onSuccess {
                _uiState.value = MuseumState.MuseumUpdated
            }.onFailure { e ->
                _uiState.value = MuseumState.Error(parseError(e))
            }
        }
    }

    private fun deleteMuseum(museumId: Int) {
        viewModelScope.launch {
            _uiState.value = MuseumState.Loading
            museumRepository.deleteMuseum(museumId)
                .onSuccess { _uiState.value = MuseumState.MuseumDeleted }
                .onFailure { e -> _uiState.value = MuseumState.Error(parseError(e)) }
        }
    }

    private fun loadAllMuseums() {
        viewModelScope.launch {
            try {
                val museums = museumRepository.getAllMuseums()
                _uiState.value = MuseumState.Success(museums)
            } catch (e: Exception) {
                _uiState.value = MuseumState.Error(parseError(e))
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
                _uiState.value = MuseumState.Error(parseError(e))
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
