package com.example.museumapp.ui.exhibits

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.museumapp.MuseumApp
import com.example.museumapp.data.cache.DataCache
import com.example.museumapp.data.model.Exhibit
import com.example.museumapp.data.repository.MuseumSpinnerItem
import com.example.museumapp.databinding.ActivityEditExhibitBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditExhibitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditExhibitBinding
    private val viewModel: ExhibitViewModel by viewModels {
        ExhibitViewModelFactory((application as MuseumApp).exhibitRepository)
    }

    private var exhibitId: Int = -1
    private var currentExhibit: Exhibit? = null
    private var museumsList = listOf<MuseumSpinnerItem>()
    private var selectedMuseumId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditExhibitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setLoading(false)
        // Получаем данные из Intent
        exhibitId = intent.getIntExtra("exhibit_id", -1)
        if (exhibitId == -1) {
            showToast("Ошибка: не передан ID экспоната")
            finish()
            return
        }

        setupToolbar()
        //fillForm(title, description, creationYear, hallNumber, museumId)
        loadMuseumsFromCache()
        loadExhibitAndFillForm()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadExhibitAndFillForm() {
        setLoading(true)

        lifecycleScope.launch {
            try {
                // Загружаем из кэша (или сети, если нет в кэше)
                val repository = (application as MuseumApp).exhibitRepository
                val exhibit = repository.getExhibitById(exhibitId)

                withContext(Dispatchers.Main) {
                    if (exhibit.isSuccess) {
                        currentExhibit = exhibit.getOrNull()
                        fillForm(exhibit.getOrNull())
                    } else {
                        showToast("Не удалось загрузить данные экспоната")
                        finish()
                    }
                    setLoading(false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Ошибка: ${e.message}")
                    setLoading(false)
                    finish()
                }
            }
        }
    }

    private fun fillForm(exhibit: Exhibit?) {
        if (exhibit == null) return
        Log.d("EditForm", "Filling form: title=${exhibit?.title}, museumId=${exhibit?.museumId}")

        binding.editTextTitle.setText(exhibit.title)
        binding.editTextDescription.setText(exhibit.description)
        binding.editTextYear.setText(exhibit.creationYear.toString())

        // 🔥 Выбираем музей в спиннере
        if (exhibit.museumId != null && museumsList.isNotEmpty()) {
            val museumIndex = museumsList.indexOfFirst { it.id == exhibit.museumId }
            if (museumIndex != -1) {
                binding.spinnerMuseum.setSelection(museumIndex)
                selectedMuseumId = exhibit.museumId
            }
        }
    }

    private fun loadMuseumsFromCache() {
        lifecycleScope.launch {
            try {
                museumsList = DataCache.getMuseums {
                    (application as MuseumApp).exhibitRepository.getMuseumsForSpinner()
                }
                setupMuseumSpinner()
            } catch (e: Exception) {
                showToast("Ошибка загрузки музеев")
            }
        }
    }

    private fun setupMuseumSpinner() {
        if (museumsList.isEmpty()) return

        val displayItems = museumsList.map { it.name }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            displayItems
        )
        binding.spinnerMuseum.adapter = adapter

        // Выберем музей позже, когда загрузится экспонат
        binding.spinnerMuseum.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedMuseumId = museumsList[position].id
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
                selectedMuseumId = null
            }
        }
    }


    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val title = binding.editTextTitle.text.toString().trim()
            val description = binding.editTextDescription.text.toString().trim()
            val yearText = binding.editTextYear.text.toString().trim()
            val hallNumber = binding.editTextHallNumber.text.toString().trim()

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

            // 🔥 Отправляем событие с hall_number вместо hall_id
            viewModel.onEvent(
                ExhibitEvent.UpdateExhibit(
                    exhibitId = exhibitId,
                    title = title,
                    description = description,
                    creationYear = creationYear,
                    museumId = selectedMuseumId!!,
                    hallNumber = hallNumber
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
                        showToast("Экспонат обновлён")
                        finish()
                    }
                    is ExhibitState.Success -> {
                        // 🔥 Скрываем прогресс после успешного обновления
                        setLoading(false)
                    }
                    else -> {}
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