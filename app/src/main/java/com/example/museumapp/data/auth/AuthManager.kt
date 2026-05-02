package com.example.museumapp.data.auth

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object AuthManager {

    private const val SUPABASE_URL = "https://bxrgvanoxllcwqvzkvny.supabase.co"
    private const val SUPABASE_ANON_KEY = "sb_publishable_JCl9V3yQIob6BLqreORDhg_bFucn7zf"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val supabase = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Auth) {
            autoLoadFromStorage = true
            autoSaveToStorage = true
        }
        install(Postgrest)
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Checking)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        scope.launch {
            // Ждём немного, чтобы сессия успела загрузиться из хранилища
            delay(1000)
            checkAuthStatus()


        }
    }

    private fun checkAuthStatus() {
        val user = supabase.auth.currentUserOrNull()   // свойство, без вызова
        _authState.value = if (user != null) {
            AuthState.Authenticated
        } else {
            AuthState.Unauthenticated
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            checkAuthStatus()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            supabase.auth.signOut()
            checkAuthStatus()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isAuthenticated(): Boolean {
        return supabase.auth.currentUserOrNull() != null
    }

    val currentUser
        get() = supabase.auth.currentUserOrNull()

    val authToken: String?
        get() = supabase.auth.currentAccessTokenOrNull()?.let { "Bearer $it" }

    fun getApiHeaders(): Map<String, String> {
        return mapOf(
            "apikey" to SUPABASE_ANON_KEY,
            "Authorization" to (authToken ?: "Bearer $SUPABASE_ANON_KEY"),
            "Content-Type" to "application/json"
        )
    }
}