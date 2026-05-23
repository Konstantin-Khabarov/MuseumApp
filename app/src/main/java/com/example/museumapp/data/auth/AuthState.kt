package com.example.museumapp.data.auth

sealed class AuthState {
    object Checking : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
}
