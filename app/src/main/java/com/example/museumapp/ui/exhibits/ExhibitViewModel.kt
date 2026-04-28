package com.example.museumapp.ui.exhibits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museumapp.data.repository.ExhibitRepository
import com.example.museumapp.ui.museums.MuseumState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExhibitViewModel(
    private val exhibitRepository: ExhibitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExhibitState>(ExhibitState.Idle)
    val uiState: StateFlow<ExhibitState> = _uiState.asStateFlow()

    // Текущие значения поиска (обновлено для трех параметров)
    private var currentTitle = ""
    private var currentAuthorName = ""
    private var currentMuseumName = ""

    init {
        loadAllExhibits()
    }

    fun onEvent(event: ExhibitEvent) {
        when (event) {
            is ExhibitEvent.SearchExhibits -> {
                // Вызываем поиск с тремя параметрами
                searchExhibits(event.title, event.authorName, event.museumName)
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

    private fun loadAllExhibits() {
        viewModelScope.launch {
            try {
                val exhibits = exhibitRepository.getAllExhibits()
                _uiState.value = ExhibitState.Success(exhibits)
            } catch (e: Exception) {
                _uiState.value = ExhibitState.Error("Ошибка загрузки: ${e.message}")
            }
        }
    }

    private fun searchExhibits(title: String, authorName: String, museumName: String) {
        currentTitle = title
        currentAuthorName = authorName
        currentMuseumName = museumName

        viewModelScope.launch {
            try {
                val exhibits = exhibitRepository.searchExhibits(
                    title = title.ifEmpty { null },
                    authorName = authorName.ifEmpty { null },
                    museumName = museumName.ifEmpty { null }
                )

                _uiState.value = if (exhibits.isEmpty()) {
                    ExhibitState.ShowMessage("Экспонаты не найдены")
                } else {
                    ExhibitState.Success(exhibits)
                }
            } catch (e: Exception) {
                _uiState.value = ExhibitState.Error("Ошибка поиска: ${e.message}")
            }
        }
    }

    private fun resetSearch() {
        // Сбрасываем все три поля
        currentTitle = ""
        currentAuthorName = ""
        currentMuseumName = ""
        _uiState.value = ExhibitState.ShowMessage("Поля поиска очищены")
        _uiState.value = ExhibitState.Idle
    }

    // Обновлено для возврата трех значений
    fun getCurrentSearchValues(): Triple<String, String, String> {
        return Triple(currentTitle, currentAuthorName, currentMuseumName)
    }
}