package com.example.museumapp.ui.exhibits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museumapp.data.repository.ExhibitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExhibitViewModel(
    private val exhibitRepository: ExhibitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExhibitState>(ExhibitState.Idle)
    val uiState: StateFlow<ExhibitState> = _uiState.asStateFlow()

    // Текущие значения поиска
    private var currentTitle = ""
    private var currentExhibitId = ""

    fun onEvent(event: ExhibitEvent) {
        when (event) {
            is ExhibitEvent.SearchExhibits -> {
                searchExhibits(event.title, event.exhibitId)
            }
            ExhibitEvent.ResetSearch -> {
                resetSearch()
            }
            ExhibitEvent.AddExhibit -> {
                _uiState.value = ExhibitState.NavigateToAddExhibit
            }
            ExhibitEvent.NavigateBack -> {
                _uiState.value = ExhibitState.NavigateBack
            }
        }
    }

    private fun searchExhibits(title: String, exhibitId: String) {
        currentTitle = title
        currentExhibitId = exhibitId

        viewModelScope.launch {
            try {
                val id = exhibitId.toIntOrNull()
                val exhibits = exhibitRepository.searchExhibits(
                    title = title.ifEmpty { null },
                    exhibitId = id
                )

                if (exhibits.isEmpty()) {
                    _uiState.value = ExhibitState.ShowMessage("Экспонаты не найдены")
                } else {
                    _uiState.value = ExhibitState.Success(exhibits)
                }
            } catch (e: Exception) {
                _uiState.value = ExhibitState.Error("Ошибка поиска: ${e.message}")
            }
        }
    }

    private fun resetSearch() {
        currentTitle = ""
        currentExhibitId = ""
        _uiState.value = ExhibitState.ShowMessage("Поля поиска очищены")
        _uiState.value = ExhibitState.Idle
    }

    fun getCurrentSearchValues(): Pair<String, String> {
        return Pair(currentTitle, currentExhibitId)
    }
}