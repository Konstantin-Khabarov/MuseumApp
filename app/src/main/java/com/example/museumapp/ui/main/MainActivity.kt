package com.example.museumapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.museumapp.data.auth.AuthManager
import com.example.museumapp.ui.exhibits.ExhibitManagementActivity
import com.example.museumapp.ui.museums.MuseumManagementActivity
import com.example.museumapp.ui.auth.LoginActivity
import com.example.museumapp.databinding.ActivityMainBinding
import com.example.museumapp.ui.auth.AuthUiState
import com.example.museumapp.ui.auth.AuthViewModel
import com.example.museumapp.ui.authors.AuthorManagementActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtonListeners()
        observeAuthState()
        observeViewModel()
    }

    private fun setupButtonListeners() {
        binding.btnLogin.setOnClickListener {
            viewModel.onLoginClicked()
        }

        binding.btnLogout.setOnClickListener {
            confirmAndLogout()
        }

        binding.cardExhibitManagement.setOnClickListener {
            viewModel.onExhibitSearchClicked()
        }

        binding.cardAuthorManagement.setOnClickListener {
            viewModel.onAuthorSearchClicked()
        }

        binding.cardMuseumManagement.setOnClickListener {
            viewModel.onMuseumInfoClicked()
        }

        // Кнопка информации о залах (пока заглушка)
        binding.cardExhibitionManagement.setOnClickListener {
            viewModel.onHallsInfoClicked()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is MainState.Idle -> {
                    }

                    is MainState.ShowMessage -> {
                        showToast(state.message)
                        viewModel.resetState()
                    }

                    is MainState.NavigateTo -> {
                        navigateToDestination(state.destination)
                        viewModel.resetState()
                    }

                    MainState.Loading -> {
                        // Можно показать ProgressBar
                    }
                }
            }
        }
    }

    private fun observeAuthState() {
        lifecycleScope.launch {
            authViewModel.uiState.collect { state ->
                when (state) {
                    is AuthUiState.Authenticated -> {
                        // ✅ Пользователь вошёл — показываем кнопку выхода
                        val userEmail = AuthManager.currentUser?.email ?: "Пользователь"
                        binding.textUserName.text = userEmail
                        binding.textUserName.visibility = View.VISIBLE
                        binding.btnLogout.visibility = View.VISIBLE
                    }
                    else -> {
                        // ❌ Гость или проверка — скрываем кнопку
                        binding.textUserName.visibility = View.GONE
                        binding.btnLogout.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun confirmAndLogout() {
        AlertDialog.Builder(this)
            .setTitle("Выход из системы")
            .setMessage("Вы действительно хотите выйти?")
            .setPositiveButton("Выйти") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun performLogout() {
        lifecycleScope.launch {
            // Показываем загрузку (опционально)
            binding.btnLogout.isEnabled = false

            val result = authViewModel.logout()

            binding.btnLogout.isEnabled = true

            result
                .onSuccess {
                    showToast("Выход выполнен")
                    // 🔥 Обновляем интерфейс: кнопка скроется автоматически через observeAuthState()
                    // 🔥 Опционально: обновить списки, если они фильтруются по правам
                    //refreshContent()
                }
                .onFailure { error ->
                    showToast("Ошибка выхода: ${error.message}")
                }
        }
    }

    private fun navigateToDestination(destination: NavigationDestination) {
        if (destination.requiresAuth && !AuthManager.isAuthenticated()) {
            showToast("Для этого действия необходимо войти в систему")
            // 🔥 Опционально: предложить войти
            // val intent = Intent(this, LoginActivity::class.java)
            // startActivity(intent)
            return
        }
        val intent = when (destination) {
            NavigationDestination.Login ->
                Intent(this, LoginActivity::class.java)

            NavigationDestination.ExhibitManagement ->
                Intent(this, ExhibitManagementActivity::class.java)

            NavigationDestination.AuthorManagement ->
                Intent(this, AuthorManagementActivity::class.java)

            NavigationDestination.MuseumManagement ->
                Intent(this, MuseumManagementActivity::class.java)

            NavigationDestination.HallManagement -> {
                // Пока заглушка - можно создать аналогичную активность
                showToast("Экран залов в разработке")
                return
            }
        }

        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}