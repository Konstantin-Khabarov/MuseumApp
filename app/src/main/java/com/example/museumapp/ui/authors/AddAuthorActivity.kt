package com.example.museumapp.ui.authors

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.museumapp.MuseumApp
import com.example.museumapp.databinding.ActivityAddAuthorBinding
import kotlinx.coroutines.launch

class AddAuthorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAuthorBinding
    private val viewModel: AuthorViewModel by viewModels {
        AuthorViewModelFactory((application as MuseumApp).authorRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAuthorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            val biography = binding.editTextBiography.text.toString().trim().takeIf { it.isNotBlank() }
            val birthDate = binding.editTextBirthDate.text.toString().trim().takeIf { it.isNotBlank() }
            val deathDate = binding.editTextDeathDate.text.toString().trim().takeIf { it.isNotBlank() }

            // Валидация
            if (name.isEmpty()) {
                binding.editTextName.error = "Обязательное поле"
                binding.editTextName.requestFocus()
                return@setOnClickListener
            }

            // Отправляем событие в ViewModel
            viewModel.onEvent(
                AuthorEvent.SaveAuthor(
                    name = name,
                    biography = biography,
                    birthDate = birthDate,
                    deathDate = deathDate
                )
            )
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is AuthorState.Loading -> {
                        setLoading(true)
                    }
                    is AuthorState.Error -> {
                        showToast(state.message)
                        setLoading(false)
                    }
                    is AuthorState.AuthorAdded -> {
                        showToast("Автор добавлен")
                        finish()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !isLoading
        binding.editTextName.isEnabled = !isLoading
        binding.editTextBiography.isEnabled = !isLoading
        binding.editTextBirthDate.isEnabled = !isLoading
        binding.editTextDeathDate.isEnabled = !isLoading
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}