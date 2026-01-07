package com.example.museumapp

import android.app.Application
import com.example.museumapp.data.repository.AuthorRepository
import com.example.museumapp.data.repository.ExhibitRepository
import com.example.museumapp.data.repository.MuseumRepository


class MuseumApp : Application() {
    val authorRepository by lazy { AuthorRepository() }
    val exhibitRepository by lazy { ExhibitRepository() }
    val museumRepository by lazy { MuseumRepository() }
}