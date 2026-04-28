package com.example.museumapp.ui.museums

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.museumapp.MuseumApp
import com.example.museumapp.data.model.Museum
import com.example.museumapp.databinding.ActivityMuseumManagementBinding
import com.example.museumapp.ui.museums.MuseumAdapter
import com.example.museumapp.ui.museums.MuseumDetailActivity
import kotlinx.coroutines.launch

class MuseumManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMuseumManagementBinding
    private lateinit var museumAdapter: MuseumAdapter
    private val viewModel: MuseumViewModel by viewModels {
        MuseumViewModelFactory((application as MuseumApp).museumRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMuseumManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupUI()
        setupObservers()
        setupListeners()
    }

    private fun setupUI() {
        // Восстановление предыдущих значений поиска
        val (name, city) = viewModel.getCurrentSearchValues()
        binding.editTextMuseumName.setText(name)
        binding.editTextMuseumCity.setText(city)
    }


    private fun setupRecyclerView() {
        museumAdapter = MuseumAdapter { museum ->
            val intent = Intent(this, MuseumDetailActivity::class.java).apply {
                putExtra("museum_id", museum.id)
                putExtra("museum_name", museum.name)
                putExtra("museum_city", museum.city)
                putExtra("museum_address", museum.address)
            }
            startActivity(intent)
        }
        binding.recyclerViewMuseums.adapter = museumAdapter
        binding.recyclerViewMuseums.layoutManager = LinearLayoutManager(this)
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
            val city = binding.editTextMuseumCity.text.toString()

            viewModel.onEvent(
                MuseumEvent.SearchMuseums(name, city)
            )
        }

        binding.btnReset.setOnClickListener {
            binding.editTextMuseumName.setText("")
            binding.editTextMuseumCity.setText("")
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

    private fun showSearchResults(museums: List<Museum>) {
        museumAdapter.submitList(museums)
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