package com.example.museumapp.ui.exhibits

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.museumapp.MuseumApp
import com.example.museumapp.data.model.Hall
import com.example.museumapp.data.repository.AuthorSpinnerItem
import com.example.museumapp.data.repository.MuseumSpinnerItem
import com.example.museumapp.databinding.ActivityAddExhibitBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddExhibitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExhibitBinding

    // 🔥 Используем тот же ViewModel (shared между экранами)
    private val viewModel: ExhibitViewModel by viewModels {
        ExhibitViewModelFactory((application as MuseumApp).exhibitRepository)
    }

    // Данные для Spinner
    private var museumsList = listOf<MuseumSpinnerItem>()
    private var hallsList = listOf<Hall>()
    private var authorsList = listOf<AuthorSpinnerItem>()

    // Выбранные значения
    private var selectedMuseumId: Int? = null
    private var selectedHallId: Int? = null
    private var selectedAuthorId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExhibitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadAllDataParallel()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            // Просто закрываем экран, данные не сохраняются
            finish()
        }
    }

    private fun loadAllDataParallel() {
        // 🔥 Не показываем ProgressBar — данные уже в кэше!
        // Или показываем на очень короткое время, если кэш ещё не готов

        lifecycleScope.launch {
            try {
                // 🔥 Мгновенное получение из кэша (или быстрая загрузка, если первый раз)
                val museums = viewModel.getMuseumsForSpinner()
                val authors = viewModel.getAuthorsForSpinner()

                withContext(Dispatchers.Main) {
                    museumsList = museums
                    authorsList = authors

                    setupMuseumSpinner(museums)
                    setupAuthorSpinner(authors)

                    // 🔥 ProgressBar можно не показывать вообще, или на 100-200мс
                    setLoading(false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Ошибка: ${e.message}")
                    setLoading(false)
                    android.util.Log.e("AddExhibit", "Load error: ${e.message}", e)
                }
            }
        }
    }

    /*private fun loadMuseums() {
        setLoading(true)

        lifecycleScope.launch {
            try {
                val museums = viewModel.getMuseumsForSpinner()

                withContext(Dispatchers.Main) {
                    museumsList = museums
                    setupMuseumSpinner(museums)
                    // Не скрываем прогресс, пока не загрузятся все справочники
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Ошибка загрузки музеев: ${e.message}")
                }
            }
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
                    setupAuthorSpinner(authors)
                    setLoading(false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Ошибка загрузки авторов: ${e.message}")
                    setLoading(false)
                }
            }
        }
    }*/

    private fun loadHalls(museumId: Int) {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val halls = withContext(Dispatchers.IO) {
                    viewModel.getHallsByMuseumId(museumId)
                }

                withContext(Dispatchers.Main) {
                    hallsList = halls
                    setupHallSpinner(halls)
                    binding.progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Ошибка загрузки залов: ${e.message}")
                    binding.progressBar.visibility = View.GONE
                    android.util.Log.e("AddExhibit", "Halls error: ${e.message}", e)
                }
            }
        }
    }

    private fun setupMuseumSpinner(museums: List<MuseumSpinnerItem>) {
        val items = listOf(MuseumSpinnerItem(-1, "Выберите музей")) + museums
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            items
        )
        binding.spinnerMuseum.adapter = adapter

        binding.spinnerMuseum.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    // "Выберите музей" — скрываем залы
                    selectedMuseumId = null
                    selectedHallId = null
                    hideHallSpinner()
                } else {
                    // Выбран музей — загружаем его залы
                    selectedMuseumId = items[position].id
                    selectedHallId = null
                    loadHalls(selectedMuseumId!!)
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
                selectedMuseumId = null
                selectedHallId = null
                hideHallSpinner()
            }
        }
    }

    // 🔥 Настройка Spinner залов (вызывается после загрузки)
    private fun setupHallSpinner(halls: List<Hall>) {
        if (halls.isEmpty()) {
            hideHallSpinner()
            showToast("В этом музее нет залов")
            return
        }
        // Показываем Spinner залов
        binding.textHallLabel.visibility = View.VISIBLE
        binding.spinnerHall.visibility = View.VISIBLE
        binding.spinnerHall.isEnabled = true

        // Добавляем пункт "Без зала" в начало
        val hallItems = listOf(Hall(-1, selectedMuseumId ?: 0, null, "Без зала", null, null)) + halls
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            hallItems.map { it.toString() }  // Используем форматированное имя
        )
        binding.spinnerHall.adapter = adapter

        binding.spinnerHall.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedHallId = if (position == 0) null else hallItems[position].hall_id
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
                selectedHallId = null
            }
        }
    }

    private fun setupAuthorSpinner(authors: List<AuthorSpinnerItem>) {
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
                selectedAuthorId = if (position == 0) null else items[position].id
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
                selectedAuthorId = null
            }
        }
    }

    private fun hideHallSpinner() {
        binding.textHallLabel.visibility = View.GONE
        binding.spinnerHall.visibility = View.GONE
        binding.spinnerHall.adapter = null
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val title = binding.editTextTitle.text.toString().trim()
            val description = binding.editTextDescription.text.toString().trim()
            val yearText = binding.editTextYear.text.toString().trim()

            // Валидация
            if (title.isEmpty()) {
                binding.editTextTitle.error = "Обязательное поле"
                binding.editTextTitle.requestFocus()
                return@setOnClickListener
            }

            if (selectedMuseumId == null) {
                showToast("Выберите музей")
                return@setOnClickListener
            }

            val creationYear = yearText.toIntOrNull() ?: 0

            // 🔥 Отправляем событие с данными в ViewModel
            viewModel.onEvent(
                ExhibitEvent.SaveExhibit(
                    title = title,
                    description = description,
                    creationYear = creationYear,
                    hallId = selectedHallId,
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
        binding.spinnerMuseum.isEnabled = !isLoading
        binding.spinnerHall.isEnabled = !isLoading && selectedMuseumId != null
        binding.spinnerAuthor.isEnabled = !isLoading
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}