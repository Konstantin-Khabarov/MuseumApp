package com.example.museumapp.ui.museums

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.museumapp.MuseumApp
import com.example.museumapp.databinding.ActivityMuseumManagementBinding
import kotlinx.coroutines.launch

class MuseumManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMuseumManagementBinding
    private val viewModel: MuseumViewModel by viewModels {
        MuseumViewModelFactory((application as MuseumApp).museumRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMuseumManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
        setupListeners()
    }

    private fun setupUI() {
        // Восстановление предыдущих значений поиска
        val (name, museumId, country) = viewModel.getCurrentSearchValues()
        binding.editTextMuseumName.setText(name)
        binding.editTextMuseumId.setText(museumId)
        binding.editTextMuseumCountry.setText(country)
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                handleState(state)
            }
        }
    }

    private fun handleState(state: MuseumState) {
        when (state) {
            is MuseumState.Idle -> {
                // Ничего не делаем
            }
            is MuseumState.Success -> {
                showSearchResults(state.museums)
            }
            is MuseumState.Error -> {
                showToast(state.message)
            }
            is MuseumState.ShowMessage -> {
                showToast(state.message)
            }
            MuseumState.NavigateBack -> {
                finish()
            }
            MuseumState.NavigateToAddMuseum -> {
                navigateToAddMuseum()
            }
            MuseumState.NavigateToEditMuseum -> {
                navigateToEditMuseum()
            }
        }
    }

    private fun setupListeners() {
        binding.btnBackArrow.setOnClickListener {
            viewModel.onEvent(MuseumEvent.NavigateBack)
        }

        binding.btnSearch.setOnClickListener {
            val name = binding.editTextMuseumName.text.toString()
            val museumId = binding.editTextMuseumId.text.toString()
            val country = binding.editTextMuseumCountry.text.toString()

            viewModel.onEvent(
                MuseumEvent.SearchMuseums(name, museumId, country)
            )
        }

        binding.btnReset.setOnClickListener {
            binding.editTextMuseumName.setText("")
            binding.editTextMuseumId.setText("")
            binding.editTextMuseumCountry.setText("")
            viewModel.onEvent(MuseumEvent.ResetSearch)
        }

        // Кнопка добавления
        binding.btnAddMuseum.setOnClickListener {
            viewModel.onEvent(MuseumEvent.AddMuseum)
        }

        // Кнопка редактирования
        /*binding.btnEditMuseum.setOnClickListener {
            viewModel.onEvent(MuseumEvent.EditMuseum)
        }*/
    }

    private fun showSearchResults(museums: List<com.example.museumapp.data.model.Museum>) {
        val message = buildString {
            append("Найдено музеев: ${museums.size}\n")
            museums.take(3).forEachIndexed { index, museum ->
                append("${index + 1}. ${museum.name} (${museum.country})\n")
            }
            if (museums.size > 3) {
                append("... и ещё ${museums.size - 3}")
            }
        }

        showToast(message)

        // binding.recyclerViewMuseums.adapter = MuseumAdapter(museums)
    }

    private fun navigateToAddMuseum() {
        showToast("Добавление нового музея")
        // startActivity(Intent(this, AddMuseumActivity::class.java))
    }

    private fun navigateToEditMuseum() {
        showToast("Редактирование информации о музее")
        // startActivity(Intent(this, EditMuseumActivity::class.java))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}