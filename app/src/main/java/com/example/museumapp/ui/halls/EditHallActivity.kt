package com.example.museumapp.ui.halls

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.museumapp.MuseumApp
import com.example.museumapp.data.auth.AuthManager
import com.example.museumapp.data.repository.MuseumSpinnerItem
import com.example.museumapp.databinding.ActivityEditHallBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditHallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditHallBinding
    private val viewModel: HallViewModel by viewModels {
        HallViewModelFactory((application as MuseumApp).hallRepository)
    }

    private var hallId: Int = -1
    private var museumsList = listOf<MuseumSpinnerItem>()
    private var selectedMuseumId: Int? = null
    private var initialMuseumId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditHallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hallId = intent.getIntExtra("hall_id", -1)
        initialMuseumId = intent.getIntExtra("hall_museum_id", -1)
        if (hallId == -1) { showToast("Ошибка: не передан ID зала"); finish(); return }

        binding.btnBack.setOnClickListener { finish() }
        fillForm()
        loadMuseums()
        setupListeners()
        observeViewModel()
    }

    private fun fillForm() {
        binding.editTextHallNumber.setText(intent.getStringExtra("hall_number") ?: "")
        binding.editTextName.setText(intent.getStringExtra("hall_name") ?: "")
        binding.editTextDescription.setText(intent.getStringExtra("hall_description") ?: "")
        binding.checkBoxIsStorage.isChecked = intent.getBooleanExtra("hall_is_storage", false)
    }

    private fun loadMuseums() {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val museums = viewModel.getMuseumsForSpinner()
                withContext(Dispatchers.Main) {
                    museumsList = museums
                    setupMuseumSpinner(museums)
                    setLoading(false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Ошибка загрузки музеев: ${e.message}")
                    setLoading(false)
                }
            }
        }
    }

    private fun setupMuseumSpinner(museums: List<MuseumSpinnerItem>) {
        val items = listOf(MuseumSpinnerItem(-1, "Выберите музей *")) + museums
        binding.spinnerMuseum.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, items
        )
        // Preselect current museum
        val idx = items.indexOfFirst { it.id == initialMuseumId }
        if (idx > 0) binding.spinnerMuseum.setSelection(idx)
        selectedMuseumId = if (initialMuseumId > 0) initialMuseumId else null

        binding.spinnerMuseum.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedMuseumId = if (position == 0) null else items[position].id
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) { selectedMuseumId = null }
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            if (!AuthManager.isAuthenticated()) {
                showToast("Для редактирования необходимо войти в систему")
                return@setOnClickListener
            }
            if (selectedMuseumId == null) {
                showToast("Выберите музей")
                return@setOnClickListener
            }
            viewModel.onEvent(
                HallEvent.UpdateHall(
                    hallId = hallId,
                    museumId = selectedMuseumId!!,
                    hallNumber = binding.editTextHallNumber.text.toString().trim().takeIf { it.isNotBlank() },
                    name = binding.editTextName.text.toString().trim().takeIf { it.isNotBlank() },
                    description = binding.editTextDescription.text.toString().trim().takeIf { it.isNotBlank() },
                    isStorage = binding.checkBoxIsStorage.isChecked
                )
            )
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is HallState.Loading -> setLoading(true)
                    is HallState.HallUpdated -> {
                        showToast("Зал обновлён")
                        setResult(RESULT_OK)
                        finish()
                    }
                    is HallState.Error -> {
                        showToast(state.message)
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
        binding.spinnerMuseum.isEnabled = !isLoading
        binding.editTextHallNumber.isEnabled = !isLoading
        binding.editTextName.isEnabled = !isLoading
        binding.editTextDescription.isEnabled = !isLoading
        binding.checkBoxIsStorage.isEnabled = !isLoading
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
