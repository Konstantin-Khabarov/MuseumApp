package com.example.museumapp.ui.authors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museumapp.data.auth.AuthManager
import com.example.museumapp.data.cache.DataCache
import com.example.museumapp.data.model.Author
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

    init {
        loadAllAuthors()
    }

    fun onEvent(event: AuthorEvent) {
        when (event) {
            is AuthorEvent.SearchAuthors -> {
                searchAuthors(event.name)
            }
            AuthorEvent.ResetSearch -> {
                resetSearch()
            }
            AuthorEvent.LoadAllAuthors -> {
                loadAllAuthors()
            }
            AuthorEvent.AddAuthor -> {
                if (AuthManager.isAuthenticated()) {
                    _uiState.value = AuthorState.NavigateToAddAuthor
                } else {
                    _uiState.value = AuthorState.ShowMessage("Необходимо войти в систему")
                }
            }
            AuthorEvent.EditAuthor -> {
                if (AuthManager.isAuthenticated()) {
                    _uiState.value = AuthorState.NavigateToEditAuthor
                } else {
                    _uiState.value = AuthorState.ShowMessage("Необходимо войти в систему")
                }
            }
            AuthorEvent.NavigateBack -> {
                _uiState.value = AuthorState.NavigateBack
            }
            is AuthorEvent.SaveAuthor -> {
                saveNewAuthor(
                    name = event.name,
                    biography = event.biography,
                    birthDate = event.birthDate,
                    deathDate = event.deathDate,
                    photoUrl = event.photoUrl
                )
            }
            is AuthorEvent.UpdateAuthor -> {
                updateAuthor(
                    authorId = event.authorId,
                    name = event.name,
                    biography = event.biography,
                    birthDate = event.birthDate,
                    deathDate = event.deathDate,
                    photoUrl = event.photoUrl
                )
            }
            is AuthorEvent.DeleteAuthor -> {
                deleteAuthor(event.authorId)
            }
        }
    }

    private fun loadAllAuthors() {
        viewModelScope.launch {
            try {
                val authors = authorRepository.getAllAuthors()
                _uiState.value = AuthorState.Success(authors)
            } catch (e: Exception) {
                _uiState.value = AuthorState.Error("Ошибка загрузки: ${e.message}")
            }
        }
    }

    private fun saveNewAuthor(
        name: String,
        biography: String?,
        birthDate: String?,
        deathDate: String?,
        photoUrl: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = AuthorState.Loading
            try {
                val newAuthor = Author(
                    id = 0,
                    name = name,
                    biography = biography,
                    birthDate = birthDate,
                    deathDate = deathDate,
                    photoUrl = photoUrl
                )
                val result = authorRepository.insertAuthor(newAuthor)
                result.onSuccess {
                    DataCache.invalidateAuthors()
                    _uiState.value = AuthorState.AuthorAdded
                }.onFailure { error ->
                    _uiState.value = AuthorState.Error("Ошибка: ${error.message}")
                }
            } catch (e: Exception) {
                _uiState.value = AuthorState.Error("Ошибка: ${e.message}")
            }
        }
    }

    private fun updateAuthor(
        authorId: Int,
        name: String,
        biography: String?,
        birthDate: String?,
        deathDate: String?,
        photoUrl: String?
    ) {
        viewModelScope.launch {
            _uiState.value = AuthorState.Loading
            try {
                val result = authorRepository.updateAuthor(
                    id = authorId,
                    name = name,
                    biography = biography,
                    birthDate = birthDate,
                    deathDate = deathDate,
                    photoUrl = photoUrl
                )
                result.onSuccess {
                    DataCache.invalidateAuthors()
                    _uiState.value = AuthorState.AuthorUpdated
                }.onFailure { error ->
                    _uiState.value = AuthorState.Error("Ошибка: ${error.message}")
                }
            } catch (e: Exception) {
                _uiState.value = AuthorState.Error("Ошибка: ${e.message}")
            }
        }
    }

    private fun searchAuthors(name: String) {
        // Сохраняем текущие значения
        currentName = name

        viewModelScope.launch {
            try {
                val authors = authorRepository.searchAuthors(
                    name = name.ifEmpty { null },
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
        loadAllAuthors()
    }
    
    private fun deleteAuthor(id: Int) {
        viewModelScope.launch {
            android.util.Log.d("DELETE_AUTHOR", "ViewModel: deleteAuthor called for id=$id")
            _uiState.value = AuthorState.Loading
            authorRepository.deleteAuthor(id)
                .onSuccess {
                    android.util.Log.d("DELETE_AUTHOR", "ViewModel: success → AuthorDeleted")
                    DataCache.invalidateAuthors()
                    _uiState.value = AuthorState.AuthorDeleted
                }
                .onFailure { e ->
                    android.util.Log.e("DELETE_AUTHOR", "ViewModel: failure → ${e.message}")
                    _uiState.value = AuthorState.Error("Ошибка удаления: ${e.message}")
                }
        }
    }
    
    fun resetState() {
        _uiState.value = AuthorState.Idle
    }

    /*fun deleteAuthor(id: Int) {
        viewModelScope.launch {
            try {
                authorRepository.deleteAuthor(id)
                // Обновить список, если нужно
                loadAllAuthors() // например, перезагрузить
            } catch (e: Exception) {
                _uiState.value = AuthorState.Error("Ошибка удаления: ${e.message}")
            }
        }
    }*/

    fun getCurrentSearchValues(): String {
        return currentName
    }
}