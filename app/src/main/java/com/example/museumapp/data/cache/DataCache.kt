package com.example.museumapp.data.cache

import com.example.museumapp.data.cache.DataCache.exhibitsMutex
import com.example.museumapp.data.model.Exhibit
import com.example.museumapp.data.repository.AuthorSpinnerItem
import com.example.museumapp.data.repository.MuseumSpinnerItem
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object DataCache {

    // 🔥 Кэш для музеев
    private var _museums: List<MuseumSpinnerItem>? = null
    private val museumsMutex = Mutex()

    // 🔥 Кэш для авторов
    private var _authors: List<AuthorSpinnerItem>? = null
    private val authorsMutex = Mutex()
    private val _exhibits = mutableMapOf<Int, Exhibit>()
    private val exhibitsMutex = Mutex()

    // 🔥 Флаг: данные загружены?
    var isInitialized = false
        private set

    // 🔥 Получение музеев (из кэша или загрузка)
    suspend fun getMuseums(loader: suspend () -> List<MuseumSpinnerItem>): List<MuseumSpinnerItem> {
        return museumsMutex.withLock {
            if (_museums != null) {
                _museums!!
            } else {
                val fresh = loader()
                _museums = fresh
                fresh
            }
        }
    }

    // 🔥 Получение авторов (из кэша или загрузка)
    suspend fun getAuthors(loader: suspend () -> List<AuthorSpinnerItem>): List<AuthorSpinnerItem> {
        return authorsMutex.withLock {
            if (_authors != null) {
                _authors!!
            } else {
                val fresh = loader()
                _authors = fresh
                fresh
            }
        }
    }
    suspend fun getExhibit(exhibitId: Int, loader: suspend () -> Exhibit?): Exhibit? {
        return exhibitsMutex.withLock {
            // Сначала ищем в кэше
            _exhibits[exhibitId] ?: run {
                // Если нет — загружаем и сохраняем
                val fresh = loader()
                if (fresh != null) {
                    _exhibits[exhibitId] = fresh
                }
                fresh
            }
        }
    }
    suspend fun cacheExhibits(exhibits: List<Exhibit>, clearPrevious: Boolean = false) {
        exhibitsMutex.withLock {
            if (clearPrevious) {
                _exhibits.clear()
            }
            exhibits.forEach { exhibit ->
                _exhibits[exhibit.id] = exhibit
            }
        }
    }
    suspend fun hasExhibit(exhibitId: Int): Boolean {
        return exhibitsMutex.withLock {
            _exhibits.containsKey(exhibitId)
        }
    }

    // 🔥 НОВЫЙ: Очистка кэша экспонатов
    fun invalidateExhibits() {

        _exhibits.clear()

    }

    // 🔥 НОВЫЙ: Удаление одного экспоната (после удаления)
    fun removeExhibit(exhibitId: Int) {

            _exhibits.remove(exhibitId)

    }

    // 🔥 НОВЫЙ: Обновление экспоната (после редактирования)
    fun updateExhibit(exhibit: Exhibit) {

            _exhibits[exhibit.id] = exhibit

    }

    // 🔥 Предзагрузка при старте приложения
    suspend fun preload(
        loadMuseums: suspend () -> List<MuseumSpinnerItem>,
        loadAuthors: suspend () -> List<AuthorSpinnerItem>
    ) {
        // Загружаем параллельно
        kotlinx.coroutines.coroutineScope {
            val museumsJob = async {
                runCatching { loadMuseums() }.getOrDefault(emptyList())
            }
            val authorsJob = async {
                runCatching { loadAuthors() }.getOrDefault(emptyList())
            }

            _museums = museumsJob.await()
            _authors = authorsJob.await()
            isInitialized = true
        }
    }

    fun invalidateMuseums() {
        _museums = null
    }

    // 🔥 Очистить только авторов
    fun invalidateAuthors() {
        _authors = null
    }
}