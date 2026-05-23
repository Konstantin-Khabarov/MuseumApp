package com.example.museumapp.data.cache

import com.example.museumapp.data.model.Exhibit
import com.example.museumapp.data.repository.AuthorSpinnerItem
import com.example.museumapp.data.repository.MuseumSpinnerItem
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object DataCache {

    private var _museums: List<MuseumSpinnerItem>? = null
    private val museumsMutex = Mutex()

    private var _authors: List<AuthorSpinnerItem>? = null
    private val authorsMutex = Mutex()
    private val _exhibits = mutableMapOf<Int, Exhibit>()
    private val exhibitsMutex = Mutex()

    var isInitialized = false
        private set

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

            _exhibits[exhibitId] ?: run {

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

    fun removeExhibit(exhibitId: Int) {
            _exhibits.remove(exhibitId)
    }

    fun updateExhibit(exhibit: Exhibit) {
            _exhibits[exhibit.id] = exhibit
    }

    suspend fun preload(
        loadMuseums: suspend () -> List<MuseumSpinnerItem>,
        loadAuthors: suspend () -> List<AuthorSpinnerItem>
    ) {

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

    fun invalidateAuthors() {
        _authors = null
    }
}
