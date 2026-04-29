package com.example.museumapp.ui.exhibits

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.museumapp.MuseumApp
import com.example.museumapp.data.repository.AuthorSpinnerItem
import com.example.museumapp.databinding.ActivityAddExhibitBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddExhibitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExhibitBinding

    // 🔥 Используем тот же ViewModel (shared между экранами)
    private val viewModel: ExhibitViewModel by viewModels {
        ExhibitViewModelFactory((application as MuseumApp).exhibitRepository)
    }

    private var authorsList = listOf<AuthorSpinnerItem>()
    private var selectedAuthorId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExhibitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadAuthors()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            // Просто закрываем экран, данные не сохраняются
            finish()
        }
    }

    private fun loadAuthors() {
        setLoading(true)

        lifecycleScope.launch {
            try {
                // Загружаем авторов через ViewModel
                val authors = viewModel.getAuthorsForSpinner()

                withContext(Dispatchers.Main) {
                    authorsList = authors
                    setupSpinner(authors)
                    setLoading(false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Ошибка загрузки авторов: ${e.message}")
                    setLoading(false)
                }
            }
        }
    }

    private fun setupSpinner(authors: List<AuthorSpinnerItem>) {
        // Добавляем пункт "Без автора" в начало
        val items = listOf(AuthorSpinnerItem(-1, "Без автора")) + authors

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            items
        )
        binding.spinnerAuthor.adapter = adapter

        binding.spinnerAuthor.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                // position 0 = "Без автора"
                selectedAuthorId = if (position == 0) null else items[position].id
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
                selectedAuthorId = null
            }
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val title = binding.editTextTitle.text.toString().trim()
            val description = binding.editTextDescription.text.toString().trim()
            val yearText = binding.editTextYear.text.toString().trim()
            val hallText = binding.editTextHallId.text.toString().trim()

            // Валидация
            if (title.isEmpty()) {
                binding.editTextTitle.error = "Обязательное поле"
                binding.editTextTitle.requestFocus()
                return@setOnClickListener
            }

            val creationYear = yearText.toIntOrNull() ?: 0
            val hallId = hallText.toIntOrNull()

            // 🔥 Отправляем событие с данными в ViewModel
            viewModel.onEvent(
                ExhibitEvent.SaveExhibit(
                    title = title,
                    description = description,
                    creationYear = creationYear,
                    hallId = hallId,
                    authorId = selectedAuthorId
                )
            )
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is ExhibitState.Loading -> {
                        setLoading(true)
                    }
                    is ExhibitState.Error -> {
                        showToast(state.message)
                        setLoading(false)
                    }
                    is ExhibitState.NavigateBack -> {
                        // 🔥 ViewModel сигнализирует: вернуться назад
                        showToast("Экспонат добавлен!")
                        finish()
                    }
                    else -> {
                        // Для других состояний не меняем UI
                    }
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !isLoading
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}