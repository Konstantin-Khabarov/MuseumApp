package com.example.museumapp.ui.halls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museumapp.data.repository.HallRepository
import com.example.museumapp.data.repository.MuseumSpinnerItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HallViewModel(private val hallRepository: HallRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<HallState>(HallState.Idle)
    val uiState: StateFlow<HallState> = _uiState.asStateFlow()

    private var currentHallNumber = ""
    private var currentName = ""
    private var currentMuseumName = ""

    init { loadAllHalls() }

    fun onEvent(event: HallEvent) {
        when (event) {
            is HallEvent.SearchHalls -> searchHalls(event.hallNumber, event.name, event.museumName)
            HallEvent.ResetSearch -> resetSearch()
            HallEvent.LoadAllHalls -> loadAllHalls()
            is HallEvent.SaveHall -> saveHall(event)
            is HallEvent.UpdateHall -> updateHall(event)
            is HallEvent.DeleteHall -> deleteHall(event.hallId)
        }
    }

    private fun loadAllHalls() {
        viewModelScope.launch {
            _uiState.value = HallState.Loading
            try {
                val halls = hallRepository.getAllHalls()
                _uiState.value = HallState.Success(halls)
            } catch (e: Exception) {
                _uiState.value = HallState.Error("Ошибка загрузки: ${e.message}")
            }
        }
    }

    private fun searchHalls(hallNumber: String, name: String, museumName: String) {
        currentHallNumber = hallNumber
        currentName = name
        currentMuseumName = museumName
        viewModelScope.launch {
            _uiState.value = HallState.Loading
            try {
                val halls = hallRepository.searchHalls(
                    hallNumber = hallNumber.ifEmpty { null },
                    name = name.ifEmpty { null },
                    museumName = museumName.ifEmpty { null }
                )
                _uiState.value = if (halls.isEmpty()) HallState.ShowMessage("Залы не найдены")
                                 else HallState.Success(halls)
            } catch (e: Exception) {
                _uiState.value = HallState.Error("Ошибка поиска: ${e.message}")
            }
        }
    }

    private fun resetSearch() {
        currentHallNumber = ""; currentName = ""; currentMuseumName = ""
        loadAllHalls()
    }

    private fun saveHall(event: HallEvent.SaveHall) {
        viewModelScope.launch {
            _uiState.value = HallState.Loading
            hallRepository.addHall(
                museumId = event.museumId,
                hallNumber = event.hallNumber,
                name = event.name,
                description = event.description,
                isStorage = event.isStorage
            ).onSuccess {
                _uiState.value = HallState.HallAdded
            }.onFailure { e ->
                _uiState.value = HallState.Error("Ошибка: ${e.message}")
            }
        }
    }

    private fun updateHall(event: HallEvent.UpdateHall) {
        viewModelScope.launch {
            _uiState.value = HallState.Loading
            hallRepository.updateHall(
                hallId = event.hallId, museumId = event.museumId,
                hallNumber = event.hallNumber, name = event.name,
                description = event.description, isStorage = event.isStorage
            ).onSuccess { _uiState.value = HallState.HallUpdated }
             .onFailure { e -> _uiState.value = HallState.Error("Ошибка: ${e.message}") }
        }
    }

    private fun deleteHall(hallId: Int) {
        viewModelScope.launch {
            _uiState.value = HallState.Loading
            hallRepository.deleteHall(hallId)
                .onSuccess { _uiState.value = HallState.HallDeleted }
                .onFailure { e -> _uiState.value = HallState.Error("Ошибка удаления: ${e.message}") }
        }
    }

    suspend fun getMuseumsForSpinner(): List<MuseumSpinnerItem> =
        hallRepository.getMuseumsForSpinner()

    fun getCurrentSearchValues(): Triple<String, String, String> =
        Triple(currentHallNumber, currentName, currentMuseumName)
}
