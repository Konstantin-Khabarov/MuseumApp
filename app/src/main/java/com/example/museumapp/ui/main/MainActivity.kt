package com.example.museumapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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

        binding.cardExhibitionManagement.setOnClickListener {
            viewModel.onHallsInfoClicked()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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

                    }
                }
            }
            }
        }
    }

    private fun observeAuthState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.uiState.collect { state ->
                when (state) {
                    is AuthUiState.Authenticated -> {
                        val userEmail = AuthManager.currentUser?.email ?: "Пользователь"
                        binding.textUserName.text = userEmail
                        binding.textUserName.visibility = View.VISIBLE
                        binding.btnLogout.visibility = View.VISIBLE
                        binding.btnLogin.visibility = View.GONE
                    }
                    else -> {
                        binding.textUserName.visibility = View.GONE
                        binding.btnLogout.visibility = View.GONE
                        binding.btnLogin.visibility = View.VISIBLE
                    }
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

            binding.btnLogout.isEnabled = false

            val result = authViewModel.logout()

            binding.btnLogout.isEnabled = true

            result
                .onSuccess {
                    showToast("Выход выполнен")

                }
                .onFailure { error ->
                    showToast("Ошибка выхода: ${error.message}")
                }
        }
    }

    private fun navigateToDestination(destination: NavigationDestination) {
        if (destination.requiresAuth && !AuthManager.isAuthenticated()) {
            showToast("Для этого действия необходимо войти в систему")

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

            NavigationDestination.HallManagement ->
                Intent(this, com.example.museumapp.ui.halls.HallManagementActivity::class.java)
        }

        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
