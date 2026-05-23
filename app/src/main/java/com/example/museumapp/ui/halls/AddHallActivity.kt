package com.example.museumapp.ui.halls

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.museumapp.MuseumApp
import com.example.museumapp.data.auth.AuthManager
import com.example.museumapp.data.repository.MuseumSpinnerItem
import com.example.museumapp.databinding.ActivityAddHallBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.museumapp.ui.main.MainActivity

class AddHallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddHallBinding
    private val viewModel: HallViewModel by viewModels {
        HallViewModelFactory((application as MuseumApp).hallRepository, (application as MuseumApp).museumRepository)
    }

    private var museumsList = listOf<MuseumSpinnerItem>()
    private var selectedMuseumId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        loadMuseums()
        setupListeners()
        observeViewModel()
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
                showToast("Для добавления необходимо войти в систему")
                return@setOnClickListener
            }
            if (selectedMuseumId == null) {
                showToast("Выберите музей")
                return@setOnClickListener
            }
            viewModel.onEvent(
                HallEvent.SaveHall(
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
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                when (state) {
                    is HallState.Loading -> setLoading(true)
                    is HallState.HallAdded -> {
                        showToast("Зал добавлен")
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
