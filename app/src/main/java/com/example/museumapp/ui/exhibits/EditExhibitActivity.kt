package com.example.museumapp.ui.exhibits

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.museumapp.MuseumApp
import com.example.museumapp.data.auth.AuthManager
import com.example.museumapp.data.model.Hall
import com.example.museumapp.data.repository.AuthorSpinnerItem
import com.example.museumapp.data.repository.MuseumSpinnerItem
import com.example.museumapp.databinding.ActivityEditExhibitBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private var hallsList = listOf<Hall>()

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
        loadAllData()
        setupSaveListener()
        observeViewModel()
    }

    private fun loadAllData() {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val repo = (application as MuseumApp).exhibitRepository
                val museums = async { repo.getMuseumsForSpinner() }
                val authors = async { repo.getAuthorsForSpinner() }
                val exhibit = async { repo.getExhibitById(exhibitId) }

                val museumsResult = museums.await()
                val authorsResult = authors.await()
                val exhibitResult = exhibit.await()

                withContext(Dispatchers.Main) {
                    museumsList = museumsResult
                    authorsList = authorsResult

                    if (exhibitResult.isSuccess) {
                        val e = exhibitResult.getOrNull()!!
                        initialMuseumId = e.museumId
                        initialHallId = e.hallId
                        initialAuthorId = e.authorId

                        binding.editTextTitle.setText(e.title)
                        binding.editTextDescription.setText(e.description)
                        binding.editTextYear.setText(e.creationYear.toString())
                        binding.editTextImageUrl.setText(e.imageUrl ?: "")

                        setupAuthorSpinner()
                        setupMuseumSpinner()
                    } else {
                        showToast("Не удалось загрузить данные"); finish()
                    }
                    setLoading(false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { showToast("Ошибка: ${e.message}"); setLoading(false); finish() }
            }
        }
    }

    private fun setupAuthorSpinner() {
        val items = listOf(AuthorSpinnerItem(-1, "Без автора")) + authorsList
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

    private fun setupMuseumSpinner() {
        val items = listOf(MuseumSpinnerItem(-1, "Выберите музей")) + museumsList
        binding.spinnerMuseum.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        val idx = items.indexOfFirst { it.id == initialMuseumId }.coerceAtLeast(0)
        binding.spinnerMuseum.setSelection(idx)
        if (idx > 0) { selectedMuseumId = items[idx].id; loadHalls(selectedMuseumId!!) }

        binding.spinnerMuseum.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>, v: View?, pos: Int, id: Long) {
                val newId = if (pos == 0) null else items[pos].id
                if (newId != selectedMuseumId) {
                    selectedMuseumId = newId; selectedHallId = null
                    if (newId != null) loadHalls(newId) else hideHallSpinner()
                }
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>) { selectedMuseumId = null; hideHallSpinner() }
        }
    }

    private fun loadHalls(museumId: Int) {
        lifecycleScope.launch {
            try {
                val halls = withContext(Dispatchers.IO) { viewModel.getHallsByMuseumId(museumId) }
                withContext(Dispatchers.Main) { hallsList = halls; setupHallSpinner(halls) }
            } catch (e: Exception) { showToast("Ошибка загрузки залов") }
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
        val idx = items.indexOfFirst { it.hall_id == initialHallId }.coerceAtLeast(0)
        binding.spinnerHall.setSelection(idx)
        selectedHallId = if (idx == 0) null else items[idx].hall_id

        binding.spinnerHall.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>, v: View?, pos: Int, id: Long) {
                selectedHallId = if (pos == 0) null else items[pos].hall_id
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
            if (!AuthManager.isAuthenticated()) { showToast("Для редактирования необходимо войти в систему"); return@setOnClickListener }
            val title = binding.editTextTitle.text.toString().trim()
            if (title.isEmpty()) { binding.editTextTitle.error = "Обязательное поле"; binding.editTextTitle.requestFocus(); return@setOnClickListener }

            viewModel.onEvent(ExhibitEvent.UpdateExhibit(
                exhibitId = exhibitId,
                title = title,
                description = binding.editTextDescription.text.toString().trim(),
                creationYear = binding.editTextYear.text.toString().toIntOrNull() ?: 0,
                hallId = selectedHallId,
                authorId = selectedAuthorId,
                imageUrl = binding.editTextImageUrl.text.toString().trim().takeIf { it.isNotBlank() }
            ))
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is ExhibitState.Loading -> setLoading(true)
                    is ExhibitState.Error -> { showToast(state.message); setLoading(false) }
                    is ExhibitState.NavigateBack -> { showToast("Экспонат обновлён"); setResult(RESULT_OK); finish() }
                    else -> setLoading(false)
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !isLoading
    }

    private fun showToast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
