package com.example.museumapp.data.auth

sealed class AuthState {
    object Checking : AuthState()          // Идёт проверка сессии
    object Authenticated : AuthState()     // Пользователь вошёл
    object Unauthenticated : AuthState()   // Гость
}