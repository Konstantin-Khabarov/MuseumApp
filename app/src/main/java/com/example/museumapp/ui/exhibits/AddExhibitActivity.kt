package com.example.museumapp.ui.exhibits

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
import com.example.museumapp.data.model.Hall
import com.example.museumapp.data.repository.AuthorSpinnerItem
import com.example.museumapp.data.repository.MuseumSpinnerItem
import com.example.museumapp.databinding.ActivityAddExhibitBinding
import com.example.museumapp.ui.main.MainActivity
import kotlinx.coroutines.launch

class AddExhibitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExhibitBinding
    private val viewModel: ExhibitViewModel by viewModels {
        ExhibitViewModelFactory(
            (application as MuseumApp).exhibitRepository,
            (application as MuseumApp).authorRepository,
            (application as MuseumApp).museumRepository,
            (application as MuseumApp).hallRepository
        )
    }

    private var museumsList = listOf<MuseumSpinnerItem>()
    private var hallsList = listOf<Hall>()
    private var authorsList = listOf<AuthorSpinnerItem>()

    private var selectedMuseumId: Int? = null
    private var selectedHallId: Int? = null
    private var selectedAuthorId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExhibitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupSaveListener()
        observeViewModel()

        viewModel.onEvent(ExhibitEvent.LoadSpinnerData)
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                when (state) {
                    is ExhibitState.Loading -> setLoading(true)
                    is ExhibitState.SpinnerDataLoaded -> {
                        museumsList = state.museums
                        authorsList = state.authors
                        setupMuseumSpinner(state.museums)
                        setupAuthorSpinner(state.authors)
                        setLoading(false)
                    }
                    is ExhibitState.HallsLoaded -> {
                        hallsList = state.halls
                        setupHallSpinner(state.halls)
                        setLoading(false)
                    }
                    is ExhibitState.NavigateBack -> {
                        showToast("Экспонат добавлен!")
                        finish()
                    }
                    is ExhibitState.Error -> {
                        showToast(state.message)
                        setLoading(false)
                    }
                    else -> {}
                }
            }
            }
        }
    }

    private fun setupMuseumSpinner(museums: List<MuseumSpinnerItem>) {
        val items = listOf(MuseumSpinnerItem(-1, "Выберите музей")) + museums
        binding.spinnerMuseum.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, items
        )
        binding.spinnerMuseum.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    selectedMuseumId = null
                    selectedHallId = null
                    hideHallSpinner()
                } else {
                    selectedMuseumId = items[position].id
                    selectedHallId = null
                    viewModel.onEvent(ExhibitEvent.LoadHallsForMuseum(selectedMuseumId!!))
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
                selectedMuseumId = null
                hideHallSpinner()
            }
        }
    }

    private fun setupHallSpinner(halls: List<Hall>) {
        if (halls.isEmpty()) {
            hideHallSpinner()
            showToast("В этом музее нет залов")
            return
        }
        binding.textHallLabel.visibility = View.VISIBLE
        binding.spinnerHall.visibility = View.VISIBLE

        val items = listOf(Hall(-1, selectedMuseumId ?: 0, null, "Без зала", null, null)) + halls
        binding.spinnerHall.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, items.map { it.toString() }
        )
        binding.spinnerHall.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedHallId = if (position == 0) null else items[position].hallId
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) { selectedHallId = null }
        }
    }

    private fun setupAuthorSpinner(authors: List<AuthorSpinnerItem>) {
        val items = listOf(AuthorSpinnerItem(-1, "Без автора")) + authors
        binding.spinnerAuthor.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, items
        )
        binding.spinnerAuthor.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedAuthorId = if (position == 0) null else items[position].id
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) { selectedAuthorId = null }
        }
    }

    private fun hideHallSpinner() {
        binding.textHallLabel.visibility = View.GONE
        binding.spinnerHall.visibility = View.GONE
        binding.spinnerHall.adapter = null
    }

    private fun setupSaveListener() {
        binding.btnSave.setOnClickListener {
            val title = binding.editTextTitle.text.toString().trim()
            if (title.isEmpty()) {
                binding.editTextTitle.error = "Обязательное поле"
                binding.editTextTitle.requestFocus()
                return@setOnClickListener
            }
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            val yearInt = binding.editTextYear.text.toString().toIntOrNull()
            if (yearInt == null || yearInt < 1 || yearInt > currentYear) {
                binding.editTextYear.error = "Укажите год от 1 до $currentYear"
                binding.editTextYear.requestFocus()
                return@setOnClickListener
            }
            if (selectedMuseumId == null) {
                showToast("Выберите музей")
                return@setOnClickListener
            }
            viewModel.onEvent(
                ExhibitEvent.SaveExhibit(
                    title = title,
                    description = binding.editTextDescription.text.toString().trim(),
                    creationYear = yearInt,
                    hallId = selectedHallId,
                    authorId = selectedAuthorId,
                    imageUrl = binding.editTextImageUrl.text.toString().trim().takeIf { it.isNotBlank() }
                )
            )
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !isLoading
        binding.spinnerMuseum.isEnabled = !isLoading
        binding.spinnerAuthor.isEnabled = !isLoading
        binding.spinnerHall.isEnabled = !isLoading && selectedMuseumId != null
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
