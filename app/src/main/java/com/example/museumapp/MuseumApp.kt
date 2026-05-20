package com.example.museumapp

import android.app.Application
import coil.Coil
import coil.ImageLoader
import com.example.museumapp.data.cache.DataCache
import com.example.museumapp.data.repository.AuthorRepository
import com.example.museumapp.data.repository.ExhibitRepository
import com.example.museumapp.data.repository.MuseumRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient


class MuseumApp : Application() {
    val authorRepository by lazy { AuthorRepository() }
    val exhibitRepository by lazy { ExhibitRepository() }
    val museumRepository by lazy { MuseumRepository() }

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        setupCoil()
        preloadReferenceData()
    }

    private fun setupCoil() {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36")
                    .build()
                chain.proceed(request)
            }
            .build()

        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .okHttpClient(okHttpClient)
                .crossfade(true)
                .build()
        )
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