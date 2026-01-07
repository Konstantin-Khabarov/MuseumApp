package com.example.museumapp.ui.exhibits

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.museumapp.MuseumApp
import com.example.museumapp.databinding.ActivityExhibitManagementBinding
import kotlinx.coroutines.launch

class ExhibitManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExhibitManagementBinding
    private val viewModel: ExhibitViewModel by viewModels {
        ExhibitViewModelFactory((application as MuseumApp).exhibitRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityExhibitManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
        setupListeners()
    }

    private fun setupUI() {
        // Восстановление предыдущих значений поиска
        val (title, exhibitId) = viewModel.getCurrentSearchValues()
        binding.editTextExhibitName.setText(title)
        binding.editTextExhibitId.setText(exhibitId)
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                handleState(state)
            }
        }
    }

    private fun handleState(state: ExhibitState) {
        when (state) {
            is ExhibitState.Idle -> {
                // Ничего не делаем
            }
            is ExhibitState.Success -> {
                showSearchResults(state.exhibits)
            }
            is ExhibitState.Error -> {
                showToast(state.message)
            }
            is ExhibitState.ShowMessage -> {
                showToast(state.message)
            }
            ExhibitState.NavigateBack -> {
                finish()
            }
            ExhibitState.NavigateToAddExhibit -> {
                navigateToAddExhibit()
            }
        }
    }

    private fun setupListeners() {
        binding.btnBackArrow.setOnClickListener {
            viewModel.onEvent(ExhibitEvent.NavigateBack)
        }

        binding.btnSearch.setOnClickListener {
            val title = binding.editTextExhibitName.text.toString()
            val exhibitId = binding.editTextExhibitId.text.toString()

            viewModel.onEvent(
                ExhibitEvent.SearchExhibits(title, exhibitId)
            )
        }

        binding.btnReset.setOnClickListener {
            binding.editTextExhibitName.setText("")
            binding.editTextExhibitId.setText("")
            viewModel.onEvent(ExhibitEvent.ResetSearch)
        }

        binding.btnAdd.setOnClickListener {
            viewModel.onEvent(ExhibitEvent.AddExhibit)
        }
    }

    private fun showSearchResults(exhibits: List<com.example.museumapp.data.model.Exhibit>) {
        val message = buildString {
            append("Найдено экспонатов: ${exhibits.size}\n")
            exhibits.take(3).forEachIndexed { index, exhibit ->
                append("${index + 1}. ${exhibit.title} (${exhibit.creationDate})\n")
            }
            if (exhibits.size > 3) {
                append("... и ещё ${exhibits.size - 3}")
            }
        }

        showToast(message)

        // binding.recyclerViewExhibits.adapter = ExhibitAdapter(exhibits)
    }

    private fun navigateToAddExhibit() {
        showToast("Добавление нового экспоната")
        // startActivity(Intent(this, AddExhibitActivity::class.java))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}