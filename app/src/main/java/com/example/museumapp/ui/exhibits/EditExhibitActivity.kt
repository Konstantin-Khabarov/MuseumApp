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
import com.example.museumapp.data.auth.AuthManager
import com.example.museumapp.data.model.Exhibit
import com.example.museumapp.data.model.Hall
import com.example.museumapp.data.repository.AuthorSpinnerItem
import com.example.museumapp.data.repository.MuseumSpinnerItem
import com.example.museumapp.databinding.ActivityEditExhibitBinding
import com.example.museumapp.ui.main.MainActivity
import kotlinx.coroutines.launch

class EditExhibitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditExhibitBinding
    private val viewModel: ExhibitViewModel by viewModels {
        ExhibitViewModelFactory(
            (application as MuseumApp).exhibitRepository,
            (application as MuseumApp).authorRepository,
            (application as MuseumApp).museumRepository,
            (application as MuseumApp).hallRepository
        )
    }

    private var exhibitId: Int = -1
    private var museumsList = listOf<MuseumSpinnerItem>()
    private var authorsList = listOf<AuthorSpinnerItem>()

    private var selectedMuseumId: Int? = null
    private var selectedHallId: Int? = null
    private var selectedAuthorId: Int? = null

    private var initialMuseumId: Int? = null
    private var initialHallId: Int? = null
    private var initialAuthorId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditExhibitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        exhibitId = intent.getIntExtra("exhibit_id", -1)
        if (exhibitId == -1) { showToast("Ошибка: не передан ID экспоната"); finish(); return }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }

        observeViewModel()
        setupSaveListener()

        viewModel.onEvent(ExhibitEvent.LoadEditFormData(exhibitId))
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                when (state) {
                    is ExhibitState.Loading -> setLoading(true)
                    is ExhibitState.EditFormLoaded -> {
                        fillForm(state.exhibit)
                        museumsList = state.museums
                        authorsList = state.authors
                        initialMuseumId = state.exhibit.museumId
                        initialHallId = state.exhibit.hallId
                        initialAuthorId = state.exhibit.authorId
                        setupAuthorSpinner(state.authors)
                        setupMuseumSpinner(state.museums)
                        setupHallSpinner(state.halls)
                        setLoading(false)
                    }
                    is ExhibitState.HallsLoaded -> {
                        setupHallSpinner(state.halls)
                        setLoading(false)
                    }
                    is ExhibitState.NavigateBack -> {
                        showToast("Экспонат обновлён")
                        setResult(RESULT_OK)
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

    private fun fillForm(exhibit: Exhibit) {
        binding.editTextTitle.setText(exhibit.title)
        binding.editTextDescription.setText(exhibit.description)
        binding.editTextYear.setText(exhibit.creationYear.toString())
        binding.editTextImageUrl.setText(exhibit.imageUrl ?: "")
    }

    private fun setupAuthorSpinner(authors: List<AuthorSpinnerItem>) {
        val items = listOf(AuthorSpinnerItem(-1, "Без автора")) + authors
        binding.spinnerAuthor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        val idx = items.indexOfFirst { it.id == initialAuthorId }.coerceAtLeast(0)
        binding.spinnerAuthor.setSelection(idx)
        selectedAuthorId = if (idx == 0) null else items[idx].id

        binding.spinnerAuthor.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>, v: View?, pos: Int, id: Long) {
                selectedAuthorId = if (pos == 0) null else items[pos].id
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>) { selectedAuthorId = null }
        }
    }

    private fun setupMuseumSpinner(museums: List<MuseumSpinnerItem>) {
        val items = listOf(MuseumSpinnerItem(-1, "Выберите музей")) + museums
        binding.spinnerMuseum.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        val idx = items.indexOfFirst { it.id == initialMuseumId }.coerceAtLeast(0)
        binding.spinnerMuseum.setSelection(idx)
        if (idx > 0) selectedMuseumId = items[idx].id

        binding.spinnerMuseum.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>, v: View?, pos: Int, id: Long) {
                val newId = if (pos == 0) null else items[pos].id
                if (newId != selectedMuseumId) {
                    selectedMuseumId = newId
                    selectedHallId = null
                    if (newId != null) {
                        viewModel.onEvent(ExhibitEvent.LoadHallsForMuseum(newId))
                    } else {
                        hideHallSpinner()
                    }
                }
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>) {
                selectedMuseumId = null
                hideHallSpinner()
            }
        }
    }

    private fun setupHallSpinner(halls: List<Hall>) {
        if (halls.isEmpty()) { hideHallSpinner(); return }
        binding.textHallLabel.visibility = View.VISIBLE
        binding.spinnerHall.visibility = View.VISIBLE

        val items = listOf(Hall(-1, 0, null, "Без зала", null, null)) + halls
        binding.spinnerHall.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, items.map { it.toString() }
        )
        val idx = items.indexOfFirst { it.hallId == initialHallId }.coerceAtLeast(0)
        binding.spinnerHall.setSelection(idx)
        selectedHallId = if (idx == 0) null else items[idx].hallId

        binding.spinnerHall.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>, v: View?, pos: Int, id: Long) {
                selectedHallId = if (pos == 0) null else items[pos].hallId
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>) { selectedHallId = null }
        }
    }

    private fun hideHallSpinner() {
        binding.textHallLabel.visibility = View.GONE
        binding.spinnerHall.visibility = View.GONE
        binding.spinnerHall.adapter = null
    }

    private fun setupSaveListener() {
        binding.btnSave.setOnClickListener {
            if (!AuthManager.isAuthenticated()) {
                showToast("Для редактирования необходимо войти в систему")
                return@setOnClickListener
            }
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
            viewModel.onEvent(
                ExhibitEvent.UpdateExhibit(
                    exhibitId = exhibitId,
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
    }

    private fun showToast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
