package com.example.museumapp.data.auth

import com.example.museumapp.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object AuthManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val supabase = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
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
            supabase.auth.sessionStatus.collect { status ->
                _authState.value = when (status) {
                    is SessionStatus.Authenticated -> AuthState.Authenticated
                    is SessionStatus.NotAuthenticated -> AuthState.Unauthenticated
                    is SessionStatus.LoadingFromStorage -> AuthState.Checking
                    is SessionStatus.NetworkError -> AuthState.Unauthenticated
                }
            }
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            supabase.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isAuthenticated(): Boolean = supabase.auth.currentUserOrNull() != null

    val currentUser get() = supabase.auth.currentUserOrNull()

    val authToken: String?
        get() = supabase.auth.currentAccessTokenOrNull()?.let { "Bearer $it" }

    fun getApiHeaders(): Map<String, String> = mapOf(
        "apikey" to BuildConfig.SUPABASE_ANON_KEY,
        "Authorization" to (authToken ?: "Bearer ${BuildConfig.SUPABASE_ANON_KEY}"),
        "Content-Type" to "application/json"
    )
}
