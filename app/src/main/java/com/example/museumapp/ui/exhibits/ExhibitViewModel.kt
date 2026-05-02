package com.example.museumapp.ui.exhibits

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museumapp.data.model.Exhibit
import com.example.museumapp.data.model.Hall
import com.example.museumapp.data.repository.AuthorSpinnerItem
import com.example.museumapp.data.repository.ExhibitRepository
import com.example.museumapp.data.repository.MuseumSpinnerItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExhibitViewModel(
    private val exhibitRepository: ExhibitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExhibitState>(ExhibitState.Idle)
    val uiState: StateFlow<ExhibitState> = _uiState.asStateFlow()

    // ==================== ПАГИНАЦИЯ ====================
    companion object {
        private const val TAG = "ExhibitViewModel"
        private const val PAGE_SIZE = 20  // Загружаем по 20 записей — решает проблему timeout
    }

    // Внутреннее состояние пагинации (не передаём в UI, работает внутри VM)
    private var currentPage = 0                    // Текущая страница (0, 1, 2...)
    private var allLoaded = false                  // Все данные загружены?
    private var isLoadingMore = false              // Идёт ли загрузка следующей страницы?

    private var currentLoadJob: Job? = null
    var currentExhibitId: Int = -1
    private var currentTitle: String? = null
    private var currentAuthorName: String? = null
    private var currentMuseumName: String? = null

    // Кэш всех загруженных экспонатов (для пагинации: добавляем новые к старым)
    private val cachedExhibits = mutableListOf<Exhibit>()

    init {
        loadExhibitsPage(page = 0, isInitialLoad = true)
    }

    fun onEvent(event: ExhibitEvent) {
        when (event) {
            is ExhibitEvent.SearchExhibits -> {
                // 🔥 Отменяем предыдущий запрос, если есть
                currentLoadJob?.cancel()

                // Сбрасываем пагинацию для нового поиска
                currentTitle = event.title.ifEmpty { null }
                currentAuthorName = event.authorName.ifEmpty { null }
                currentMuseumName = event.museumName.ifEmpty { null }
                currentPage = 0
                allLoaded = false
                cachedExhibits.clear()

                performSearch(
                    title = currentTitle,
                    authorName = currentAuthorName,
                    museumName = currentMuseumName,
                    page = 0
                )
            }
            ExhibitEvent.ResetSearch -> {
                currentLoadJob?.cancel()
                resetSearch()
            }
            ExhibitEvent.NavigateToAddExhibit -> {
                // Просто отправляем сигнал навигации
                _uiState.value = ExhibitState.NavigateToAddExhibit
            }
            ExhibitEvent.ClearNavigationState -> {
                // Возвращаемся к Idle или Success (чтобы StateFlow увидел изменение)
                _uiState.value = if (cachedExhibits.isNotEmpty()) {
                    ExhibitState.Success(cachedExhibits.toList())
                } else {
                    ExhibitState.Idle
                }
            }
            is ExhibitEvent.SaveExhibit -> {
                addNewExhibit(
                    title = event.title,
                    description = event.description,
                    creationYear = event.creationYear,
                    hallId = event.hallId,
                    authorId = event.authorId
                )
            }
            is ExhibitEvent.UpdateExhibit -> {
                updateExhibit(
                    exhibitId = event.exhibitId,
                    title = event.title,
                    description = event.description,
                    creationYear = event.creationYear,
                    museumId = event.museumId,
                    hallNumber = event.hallNumber
                )
            }
            is ExhibitEvent.DeleteExhibit -> {
                deleteExhibit(event.exhibitId)
            }
            ExhibitEvent.NavigateBack -> {
                _uiState.value = ExhibitState.NavigateBack
            }
        }
    }

    private fun deleteExhibit(exhibitId: Int) {
        viewModelScope.launch {
            _uiState.value = ExhibitState.Loading  // Показываем загрузку

            try {
                val result = exhibitRepository.deleteExhibit(exhibitId)

                result
                    .onSuccess {
                        // 🔥 Успех: удаляем из кэша и уведомляем UI
                        exhibitRepository.removeExhibitFromCache(exhibitId)
                        _uiState.value = ExhibitState.NavigateBack
                    }
                    .onFailure { error ->
                        _uiState.value = ExhibitState.Error("Ошибка удаления: ${error.message}")
                    }

            } catch (e: Exception) {
                _uiState.value = ExhibitState.Error("Ошибка: ${e.message}")
            }
        }
    }

    private fun updateExhibit(
        exhibitId: Int,
        title: String,
        description: String,
        creationYear: Int,
        museumId: Int,
        hallNumber: String
    ) {
        viewModelScope.launch {
            _uiState.value = ExhibitState.Loading

            try {
                val result = exhibitRepository.updateExhibit(
                    exhibitId = exhibitId,
                    name = title,
                    description = description,
                    creationYear = creationYear,
                    museumId = museumId,
                    hallNumber = hallNumber
                )

                result
                    .onSuccess { updatedExhibit ->
                        // Обновляем в кэше
                        exhibitRepository.updateExhibitInCache(updatedExhibit)

                        // Обновляем список в кэше
                        // (если вы храните список отдельно)
                        _uiState.value = ExhibitState.NavigateBack
                    }
                    .onFailure { error ->
                        _uiState.value = ExhibitState.Error("Ошибка обновления: ${error.message}")
                    }
            } catch (e: Exception) {
                _uiState.value = ExhibitState.Error("Ошибка: ${e.message}")
            }
        }
    }

    fun loadNextPage() {
        // Защита от повторных запросов
        if (isLoadingMore || allLoaded) {
            Log.d(TAG, "Pagination: skip (isLoading=$isLoadingMore, allLoaded=$allLoaded)")
            return
        }

        val nextPage = currentPage + 1
        Log.d(TAG, "Pagination: loading page $nextPage")

        // Если активен поиск — грузим следующую страницу с теми же фильтрами
        if (currentTitle != null || currentAuthorName != null || currentMuseumName != null) {
            performSearch(
                title = currentTitle,
                authorName = currentAuthorName,
                museumName = currentMuseumName,
                page = nextPage
            )
        } else {
            // Иначе грузим следующую страницу всех экспонатов
            loadExhibitsPage(page = nextPage, isInitialLoad = false)
        }
    }

    private fun loadExhibitsPage(page: Int, isInitialLoad: Boolean) {
        currentLoadJob?.cancel()
        currentLoadJob = viewModelScope.launch {
            // Показываем загрузку только при первоначальной загрузке
            if (isInitialLoad || page == 0) {
                _uiState.value = ExhibitState.Loading
            }
            isLoadingMore = true

            try {
                // 🔥 Важно: Repository должен использовать пагинацию!
                val newExhibits = exhibitRepository.getExhibitsPage(
                    page = page,
                    pageSize = PAGE_SIZE
                )

                if (isInitialLoad) {
                    cachedExhibits.clear()
                }
                cachedExhibits.addAll(newExhibits)

                currentPage = page
                allLoaded = newExhibits.size < PAGE_SIZE
                isLoadingMore = false

                Log.d(TAG, "Loaded page $page: ${newExhibits.size} items, total: ${cachedExhibits.size}")

                _uiState.value = ExhibitState.Success(cachedExhibits.toList())

            } catch (e: Exception) {
                Log.e(TAG, "Error loading exhibits page $page", e)
                isLoadingMore = false

                // При ошибке на первой странице показываем ошибку, при подгрузке — оставляем старые данные
                if (isInitialLoad) {
                    _uiState.value = ExhibitState.Error("Ошибка загрузки: ${e.message}")
                } else {
                    // Можно показать мягкое уведомление, но не прерывать список
                    _uiState.value = ExhibitState.ShowMessage("Не удалось загрузить ещё: ${e.message}")
                }
            }
        }
    }

    private fun performSearch(
        title: String?,
        authorName: String?,
        museumName: String?,
        page: Int
    ) {
        currentLoadJob?.cancel()
        viewModelScope.launch {
            // Показываем загрузку только при первом запросе поиска
            if (page == 0) {
                _uiState.value = ExhibitState.Idle
            }
            isLoadingMore = true

            try {
                // Repository должен поддерживать пагинацию в поиске!
                val newExhibits = exhibitRepository.searchExhibits(
                    title = title,
                    authorName = authorName,
                    museumName = museumName,
                    page = page,
                    pageSize = PAGE_SIZE
                )

                if (page == 0) {
                    // Новый поиск — заменяем кэш
                    cachedExhibits.clear()
                }
                    // Подгрузка результатов поиска — добавляем к кэшу
                cachedExhibits.addAll(newExhibits)


                // Сохраняем параметры поиска для следующих страниц
                currentTitle = title
                currentAuthorName = authorName
                currentMuseumName = museumName

                // Обновляем состояние пагинации
                currentPage = page
                allLoaded = newExhibits.size < PAGE_SIZE
                isLoadingMore = false

                Log.d(TAG, "Search: loaded ${newExhibits.size} exhibits (page $page), total: ${cachedExhibits.size}")

                // Отправляем результат в UI
                _uiState.value = if (cachedExhibits.isEmpty() && page == 0) {
                    ExhibitState.ShowMessage("Экспонаты не найдены")
                } else {
                    ExhibitState.Success(cachedExhibits.toList())
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error searching exhibits", e)
                isLoadingMore = false

                if (page == 0) {
                    _uiState.value = ExhibitState.Error("Ошибка поиска: ${e.message}")
                } else {
                    _uiState.value = ExhibitState.ShowMessage("Не удалось загрузить ещё: ${e.message}")
                }
            }
        }
    }

    fun loadExhibitDetails(exhibitId: Int) {
        viewModelScope.launch {
            _uiState.value = ExhibitState.Loading // Можно убрать, так как будет мгновенно

            // 🔥 Вызываем метод репозитория
            val result = exhibitRepository.getExhibitById(exhibitId)

            result.onSuccess { exhibit ->
                // 🔥 Эмитим состояние с полными данными
                _uiState.value = ExhibitState.ExhibitDetailsLoaded(exhibit)
            }.onFailure { error ->
                // Если кэш пуст (редкий кейс), можно попробовать загрузить из сети
                // или показать ошибку
                _uiState.value = ExhibitState.Error("Данные не найдены. Обновите список.")
            }
        }
    }

    private fun resetSearch() {
        Log.d(TAG, "Reset search called")

        currentLoadJob?.cancel()

        currentTitle = null
        currentAuthorName = null
        currentMuseumName = null
        currentPage = 0
        allLoaded = false
        isLoadingMore = false
        cachedExhibits.clear()

        //_uiState.value = ExhibitState.ShowMessage("Поиск сброшен")

        loadExhibitsPage(page = 0, isInitialLoad = true)
    }

    // Обновлено для возврата трех значений
    fun getCurrentSearchValues(): Triple<String, String, String> {
        return Triple(
            currentTitle.orEmpty(),
            currentAuthorName.orEmpty(),
            currentMuseumName.orEmpty()
        )
    }

    fun canLoadMore(): Boolean {
        return !allLoaded && !isLoadingMore
    }

    private fun addNewExhibit(
        title: String,
        description: String,
        creationYear: Int,
        hallId: Int?,
        authorId: Int?
    ) {
        viewModelScope.launch {
            _uiState.value = ExhibitState.Loading  // Если добавили это состояние

            try {
                // Валидация
                if (title.isBlank()) {
                    _uiState.value = ExhibitState.Error("Название экспоната обязательно")
                    return@launch
                }

                val newExhibit = exhibitRepository.addExhibit(
                    name = title,
                    description = description,
                    creationYear = creationYear,
                    hallId = hallId,
                    authorId = authorId
                )

                // 🔥 Добавляем новый экспонат в кэш и уведомляем UI
                cachedExhibits.add(0, newExhibit)  // В начало списка
                _uiState.value = ExhibitState.Success(cachedExhibits.toList())

                // Можно отправить событие навигации назад
                _uiState.value = ExhibitState.NavigateBack

            } catch (e: Exception) {
                Log.e(TAG, "Error adding exhibit", e)
                _uiState.value = ExhibitState.Error("Ошибка: ${e.message}")
            }
        }
    }

    suspend fun getAuthorsForSpinner(): List<AuthorSpinnerItem> {
        return exhibitRepository.getAuthorsForSpinner()
    }
    suspend fun getMuseumsForSpinner(): List<MuseumSpinnerItem> {
        return exhibitRepository.getMuseumsForSpinner()
    }
    suspend fun getHallsByMuseumId(museumId: Int): List<Hall> {
        return exhibitRepository.getHallsByMuseumId(museumId)
    }
}