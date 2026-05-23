package com.example.museumapp.ui.exhibits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museumapp.data.model.Exhibit
import com.example.museumapp.data.repository.AuthorRepository
import com.example.museumapp.data.repository.ExhibitRepository
import com.example.museumapp.data.repository.HallRepository
import com.example.museumapp.data.repository.MuseumRepository
import com.example.museumapp.util.parseError
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExhibitViewModel(
    private val exhibitRepository: ExhibitRepository,
    private val authorRepository: AuthorRepository,
    private val museumRepository: MuseumRepository,
    private val hallRepository: HallRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExhibitState>(ExhibitState.Idle)
    val uiState: StateFlow<ExhibitState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<ExhibitNavigationEvent>()
    val navigationEvent: SharedFlow<ExhibitNavigationEvent> = _navigationEvent.asSharedFlow()

    private var hallLoadJob: Job? = null

    companion object {
        private const val PAGE_SIZE = 20
    }

    private var currentPage = 0
    private var allLoaded = false
    private var isLoadingMore = false

    private var currentLoadJob: Job? = null
    private var currentTitle: String? = null
    private var currentAuthorName: String? = null
    private var currentMuseumName: String? = null

    private val cachedExhibits = mutableListOf<Exhibit>()

    init {
        loadExhibitsPage(page = 0, isInitialLoad = true)
    }

    fun onEvent(event: ExhibitEvent) {
        when (event) {
            is ExhibitEvent.SearchExhibits -> {

                currentLoadJob?.cancel()

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
                viewModelScope.launch { _navigationEvent.emit(ExhibitNavigationEvent.ToAddExhibit) }
            }
            is ExhibitEvent.SaveExhibit -> {
                addNewExhibit(
                    title = event.title,
                    description = event.description,
                    creationYear = event.creationYear,
                    hallId = event.hallId,
                    authorId = event.authorId,
                    imageUrl = event.imageUrl
                )
            }
            is ExhibitEvent.UpdateExhibit -> {
                updateExhibit(
                    exhibitId = event.exhibitId,
                    title = event.title,
                    description = event.description,
                    creationYear = event.creationYear,
                    hallId = event.hallId,
                    authorId = event.authorId,
                    imageUrl = event.imageUrl
                )
            }
            is ExhibitEvent.DeleteExhibit -> {
                deleteExhibit(event.exhibitId)
            }
            is ExhibitEvent.FetchAuthorForNav -> fetchAuthorForNav(event.authorId)
            is ExhibitEvent.FetchMuseumForNav -> fetchMuseumForNav(event.museumId)
            is ExhibitEvent.FetchHallForNav -> fetchHallForNav(event.hallId)
            ExhibitEvent.NavigateBack -> _uiState.value = ExhibitState.NavigateBack
            ExhibitEvent.LoadSpinnerData -> loadSpinnerData()
            is ExhibitEvent.LoadEditFormData -> loadEditFormData(event.exhibitId)
            is ExhibitEvent.LoadHallsForMuseum -> loadHallsForMuseum(event.museumId)
        }
    }

    private fun deleteExhibit(exhibitId: Int) {
        viewModelScope.launch {
            _uiState.value = ExhibitState.Loading

            try {
                val result = exhibitRepository.deleteExhibit(exhibitId)

                result
                    .onSuccess {

                        exhibitRepository.removeExhibitFromCache(exhibitId)
                        _uiState.value = ExhibitState.NavigateBack
                    }
                    .onFailure { error ->
                        _uiState.value = ExhibitState.Error(parseError(error))
                    }

            } catch (e: Exception) {
                _uiState.value = ExhibitState.Error(parseError(e))
            }
        }
    }

    private fun updateExhibit(
        exhibitId: Int,
        title: String,
        description: String,
        creationYear: Int,
        hallId: Int?,
        authorId: Int?,
        imageUrl: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = ExhibitState.Loading

            try {
                val result = exhibitRepository.updateExhibit(
                    exhibitId = exhibitId,
                    name = title,
                    description = description,
                    creationYear = creationYear,
                    hallId = hallId,
                    authorId = authorId,
                    imageUrl = imageUrl
                )

                result
                    .onSuccess { updatedExhibit ->
                        exhibitRepository.updateExhibitInCache(updatedExhibit)
                        _uiState.value = ExhibitState.NavigateBack
                    }
                    .onFailure { error ->
                        _uiState.value = ExhibitState.Error(parseError(error))
                    }
            } catch (e: Exception) {
                _uiState.value = ExhibitState.Error(parseError(e))
            }
        }
    }

    fun loadNextPage() {
        if (isLoadingMore || allLoaded) {
            return
        }

        val nextPage = currentPage + 1

        if (currentTitle != null || currentAuthorName != null || currentMuseumName != null) {
            performSearch(
                title = currentTitle,
                authorName = currentAuthorName,
                museumName = currentMuseumName,
                page = nextPage
            )
        } else {
            loadExhibitsPage(page = nextPage, isInitialLoad = false)
        }
    }

    private fun loadExhibitsPage(page: Int, isInitialLoad: Boolean) {
        currentLoadJob?.cancel()
        currentLoadJob = viewModelScope.launch {

            if (isInitialLoad || page == 0) {
                _uiState.value = ExhibitState.Loading
            }
            isLoadingMore = true

            try {
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

                _uiState.value = ExhibitState.Success(cachedExhibits.toList())

            } catch (e: Exception) {
                isLoadingMore = false

                if (isInitialLoad) {
                    _uiState.value = ExhibitState.Error(parseError(e))
                } else {
                    _uiState.value = ExhibitState.ShowMessage("Не удалось загрузить ещё: ${parseError(e)}")
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
        currentLoadJob = viewModelScope.launch {

            if (page == 0) {
                _uiState.value = ExhibitState.Loading
            }
            isLoadingMore = true

            try {

                val newExhibits = exhibitRepository.searchExhibits(
                    title = title,
                    authorName = authorName,
                    museumName = museumName,
                    page = page,
                    pageSize = PAGE_SIZE
                )

                if (page == 0) {

                    cachedExhibits.clear()
                }

                cachedExhibits.addAll(newExhibits)

                currentTitle = title
                currentAuthorName = authorName
                currentMuseumName = museumName

                currentPage = page
                allLoaded = newExhibits.size < PAGE_SIZE
                isLoadingMore = false

                _uiState.value = if (cachedExhibits.isEmpty() && page == 0) {
                    ExhibitState.ShowMessage("Экспонаты не найдены")
                } else {
                    ExhibitState.Success(cachedExhibits.toList())
                }

            } catch (e: Exception) {
                isLoadingMore = false

                if (page == 0) {
                    _uiState.value = ExhibitState.Error(parseError(e))
                } else {
                    _uiState.value = ExhibitState.ShowMessage("Не удалось загрузить ещё: ${parseError(e)}")
                }
            }
        }
    }

    fun loadExhibitDetails(exhibitId: Int) {
        viewModelScope.launch {
            _uiState.value = ExhibitState.Loading

            val result = exhibitRepository.getExhibitById(exhibitId)

            result.onSuccess { exhibit ->

                _uiState.value = ExhibitState.ExhibitDetailsLoaded(exhibit)
            }.onFailure { error ->

                _uiState.value = ExhibitState.Error("Данные не найдены. Обновите список.")
            }
        }
    }

    private fun resetSearch() {

        currentLoadJob?.cancel()

        currentTitle = null
        currentAuthorName = null
        currentMuseumName = null
        currentPage = 0
        allLoaded = false
        isLoadingMore = false
        cachedExhibits.clear()

        loadExhibitsPage(page = 0, isInitialLoad = true)
    }

    private fun fetchAuthorForNav(authorId: Int) {
        viewModelScope.launch {
            try {
                val author = authorRepository.getAuthorById(authorId)
                if (author != null) _navigationEvent.emit(ExhibitNavigationEvent.ToAuthor(author))
            } catch (e: Exception) {
                _uiState.value = ExhibitState.Error(parseError(e))
            }
        }
    }

    private fun fetchMuseumForNav(museumId: Int) {
        viewModelScope.launch {
            try {
                val museum = museumRepository.getMuseumById(museumId)
                if (museum != null) _navigationEvent.emit(ExhibitNavigationEvent.ToMuseum(museum))
            } catch (e: Exception) {
                _uiState.value = ExhibitState.Error(parseError(e))
            }
        }
    }

    private fun fetchHallForNav(hallId: Int) {
        viewModelScope.launch {
            try {
                val hall = hallRepository.getHallById(hallId)
                if (hall != null) _navigationEvent.emit(ExhibitNavigationEvent.ToHall(hall))
            } catch (e: Exception) {
                _uiState.value = ExhibitState.Error(parseError(e))
            }
        }
    }

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
        authorId: Int?,
        imageUrl: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = ExhibitState.Loading

            try {

                if (title.isBlank()) {
                    _uiState.value = ExhibitState.Error("Название экспоната обязательно")
                    return@launch
                }

                val newExhibit = exhibitRepository.addExhibit(
                    name = title,
                    description = description,
                    creationYear = creationYear,
                    hallId = hallId,
                    authorId = authorId,
                    imageUrl = imageUrl
                )

                cachedExhibits.add(0, newExhibit)
                _uiState.value = ExhibitState.Success(cachedExhibits.toList())

                _uiState.value = ExhibitState.NavigateBack

            } catch (e: Exception) {
                _uiState.value = ExhibitState.Error(parseError(e))
            }
        }
    }

    private fun loadSpinnerData() {
        viewModelScope.launch {
            _uiState.value = ExhibitState.Loading
            try {
                val authors = exhibitRepository.getAuthorsForSpinner()
                val museums = exhibitRepository.getMuseumsForSpinner()
                _uiState.value = ExhibitState.SpinnerDataLoaded(authors, museums)
            } catch (e: Exception) {
                _uiState.value = ExhibitState.Error(parseError(e))
            }
        }
    }

    private fun loadEditFormData(exhibitId: Int) {
        viewModelScope.launch {
            _uiState.value = ExhibitState.Loading
            try {
                val exhibitResult = exhibitRepository.getExhibitById(exhibitId)
                val authors = exhibitRepository.getAuthorsForSpinner()
                val museums = exhibitRepository.getMuseumsForSpinner()

                val exhibit = exhibitResult.getOrElse {
                    _uiState.value = ExhibitState.Error("Не удалось загрузить экспонат")
                    return@launch
                }

                val halls = exhibit.museumId?.let { exhibitRepository.getHallsByMuseumId(it) } ?: emptyList()
                _uiState.value = ExhibitState.EditFormLoaded(exhibit, authors, museums, halls)
            } catch (e: Exception) {
                _uiState.value = ExhibitState.Error(parseError(e))
            }
        }
    }

    private fun loadHallsForMuseum(museumId: Int) {
        hallLoadJob?.cancel()
        hallLoadJob = viewModelScope.launch {
            try {
                val halls = exhibitRepository.getHallsByMuseumId(museumId)
                _uiState.value = ExhibitState.HallsLoaded(halls)
            } catch (e: Exception) {
                _uiState.value = ExhibitState.Error(parseError(e))
            }
        }
    }
}
