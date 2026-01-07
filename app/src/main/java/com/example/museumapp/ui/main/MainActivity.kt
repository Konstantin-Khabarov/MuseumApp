package com.example.museumapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.museumapp.ui.exhibits.ExhibitManagementActivity
import com.example.museumapp.ui.museums.MuseumManagementActivity
import com.example.museumapp.RegistrationActivity
import com.example.museumapp.databinding.ActivityMainBinding
import com.example.museumapp.ui.authors.AuthorManagementActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // View Binding
    private lateinit var binding: ActivityMainBinding

    // ViewModel
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Настройка слушателей кнопок
        setupButtonListeners()

        // Наблюдение за состоянием ViewModel
        observeViewModel()
    }

    private fun setupButtonListeners() {
        // Кнопка регистрации
        binding.btnRegistration.setOnClickListener {
            viewModel.onRegistrationClicked()
        }

        // Кнопка поиска экспоната
        binding.cardExhibitManagement.setOnClickListener {
            viewModel.onExhibitSearchClicked()
        }

        // Кнопка поиска автора
        binding.cardAuthorManagement.setOnClickListener {
            viewModel.onAuthorSearchClicked()
        }

        // Кнопка информации о музее
        binding.cardMuseumManagement.setOnClickListener {
            viewModel.onMuseumInfoClicked()
        }

        // Кнопка информации о выставках (пока заглушка)
        binding.cardExhibitionManagement.setOnClickListener {
            viewModel.onExhibitionsInfoClicked()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is MainState.Idle -> {
                        // Ничего не делаем, ждем действий пользователя
                    }

                    is MainState.ShowMessage -> {
                        showToast(state.message)
                        // После показа сообщения сбрасываем состояние
                        viewModel.resetState()
                    }

                    is MainState.NavigateTo -> {
                        navigateToDestination(state.destination)
                        // После навигации сбрасываем состояние
                        viewModel.resetState()
                    }

                    MainState.Loading -> {
                        // Можно показать ProgressBar
                    }
                }
            }
        }
    }

    private fun navigateToDestination(destination: NavigationDestination) {
        val intent = when (destination) {
            NavigationDestination.Registration ->
                Intent(this, RegistrationActivity::class.java)

            NavigationDestination.ExhibitManagement ->
                Intent(this, ExhibitManagementActivity::class.java)

            NavigationDestination.AuthorManagement ->
                Intent(this, AuthorManagementActivity::class.java)

            NavigationDestination.MuseumManagement ->
                Intent(this, MuseumManagementActivity::class.java)

            NavigationDestination.ExhibitionManagement -> {
                // Пока заглушка - можно создать аналогичную активность
                showToast("Экран выставок в разработке")
                return
            }
        }

        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}