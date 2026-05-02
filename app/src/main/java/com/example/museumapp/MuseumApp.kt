package com.example.museumapp

import android.app.Application
import com.example.museumapp.data.cache.DataCache
import com.example.museumapp.data.repository.AuthorRepository
import com.example.museumapp.data.repository.ExhibitRepository
import com.example.museumapp.data.repository.MuseumRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class MuseumApp : Application() {
    val authorRepository by lazy { AuthorRepository() }
    val exhibitRepository by lazy { ExhibitRepository() }
    val museumRepository by lazy { MuseumRepository() }

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // 🔥 Предзагружаем справочники в фоне
        preloadReferenceData()
    }

    private fun preloadReferenceData() {
        applicationScope.launch {
            try {
                DataCache.preload(
                    loadMuseums = { exhibitRepository.getMuseumsForSpinner() },
                    loadAuthors = { exhibitRepository.getAuthorsForSpinner() }
                )

                android.util.Log.d("MuseumApp", "Reference data preloaded: museums=${DataCache::class.java.getDeclaredField("_museums").let { it.isAccessible = true; it.get(DataCache) }}, authors=...")
            } catch (e: Exception) {
                android.util.Log.e("MuseumApp", "Preload failed: ${e.message}", e)
                // Не критично — данные загрузятся при первом запросе
            }
        }
    }
}