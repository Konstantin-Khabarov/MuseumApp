package com.example.museumapp.ui.exhibits

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museumapp.data.model.Exhibit
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

    // ==================== ПАГИНАЦИЯ ====================
    companion object {
        private const val TAG = "ExhibitViewModel"
        private const val PAGE_SIZE = 50  // Загружаем по 50 записей — решает проблему timeout
    }

    // Внутреннее состояние пагинации (не передаём в UI, работает внутри VM)
    private var currentPage = 0                    // Текущая страница (0, 1, 2...)
    private var allLoaded = false                  // Все данные загружены?
    private var isLoadingMore = false              // Идёт ли загрузка следующей страницы?

    private var currentTitle: String? = null
    private var currentAuthorName: String? = null
    private var currentMuseumName: String? = null

    // Кэш всех загруженных экспонатов (для пагинации: добавляем новые к старым)
    private val cachedExhibits = mutableListOf<Exhibit>()

    init {
        loadAllExhibits()
    }

    fun onEvent(event: ExhibitEvent) {
        when (event) {
            is ExhibitEvent.SearchExhibits -> {
                // Вызываем поиск с тремя параметрами
                //searchExhibits(event.title, event.authorName, event.museumName)
                performSearch(
                    title = event.title.ifEmpty { null },
                    authorName = event.authorName.ifEmpty { null },
                    museumName = event.museumName.ifEmpty { null },
                    page = 0  // Начинаем с первой страницы
                )
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
        viewModelScope.launch {
            // Показываем загрузку только при первоначальной загрузке
            if (isInitialLoad) {
                _uiState.value = ExhibitState.Idle // Можно добавить состояние Loading, если нужно
            }
            isLoadingMore = true

            try {
                // Repository должен поддерживать пагинацию! См. примечание ниже ↓
                val newExhibits = exhibitRepository.getExhibitsPage(
                    page = page,
                    pageSize = PAGE_SIZE
                )

                if (isInitialLoad) {
                    // Первая загрузка — заменяем кэш
                    cachedExhibits.clear()
                    cachedExhibits.addAll(newExhibits)
                } else {
                    // Подгрузка — добавляем к кэшу
                    cachedExhibits.addAll(newExhibits)
                }

                // Обновляем внутреннее состояние пагинации
                currentPage = page
                allLoaded = newExhibits.size < PAGE_SIZE  // Если пришло меньше 50 — больше нет данных
                isLoadingMore = false

                Log.d(TAG, "Loaded ${newExhibits.size} exhibits (page $page), total cached: ${cachedExhibits.size}, allLoaded: $allLoaded")

                // Отправляем в UI обновлённый список
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
                    cachedExhibits.addAll(newExhibits)
                } else {
                    // Подгрузка результатов поиска — добавляем к кэшу
                    cachedExhibits.addAll(newExhibits)
                }

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
        // Сбрасываем параметры поиска
        currentTitle = null
        currentAuthorName = null
        currentMuseumName = null

        // Сбрасываем пагинацию
        currentPage = 0
        allLoaded = false
        isLoadingMore = false
        cachedExhibits.clear()

        // Показываем сообщение и перезагружаем все экспонаты
        _uiState.value = ExhibitState.ShowMessage("Поля поиска очищены")
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

    fun getExhibitById(id: Int): Exhibit? {
        return cachedExhibits.find { it.id == id }
    }
}