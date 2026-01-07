package com.example.museumapp.ui.authors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museumapp.data.repository.AuthorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthorViewModel(
    private val authorRepository: AuthorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthorState>(AuthorState.Idle)
    val uiState: StateFlow<AuthorState> = _uiState.asStateFlow()

    // Поля поиска для сохранения состояния
    private var currentName = ""
    private var currentAuthorId = ""

    fun onEvent(event: AuthorEvent) {
        when (event) {
            is AuthorEvent.SearchAuthors -> {
                searchAuthors(event.name, event.authorId)
            }
            AuthorEvent.ResetSearch -> {
                resetSearch()
            }
            AuthorEvent.AddAuthor -> {
                _uiState.value = AuthorState.NavigateToAddAuthor
            }
            AuthorEvent.EditAuthor -> {
                _uiState.value = AuthorState.NavigateToEditAuthor
            }
            AuthorEvent.NavigateBack -> {
                _uiState.value = AuthorState.NavigateBack
            }
        }
    }

    private fun searchAuthors(name: String, authorId: String) {
        // Сохраняем текущие значения
        currentName = name
        currentAuthorId = authorId

        viewModelScope.launch {
            try {
                val id = authorId.toIntOrNull()
                val authors = authorRepository.searchAuthors(
                    name = name.ifEmpty { null },
                    authorId = id
                )

                if (authors.isEmpty()) {
                    _uiState.value = AuthorState.ShowMessage("Авторы не найдены")
                } else {
                    _uiState.value = AuthorState.Success(authors)
                }
            } catch (e: Exception) {
                _uiState.value = AuthorState.Error("Ошибка поиска: ${e.message}")
            }
        }
    }

    private fun resetSearch() {
        currentName = ""
        currentAuthorId = ""
        _uiState.value = AuthorState.ShowMessage("Поля поиска очищены")
        _uiState.value = AuthorState.Idle
    }

    fun getCurrentSearchValues(): Pair<String, String> {
        return Pair(currentAuthorId, currentName)
    }
}