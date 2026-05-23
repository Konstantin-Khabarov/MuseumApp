package com.example.museumapp.ui.authors

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.museumapp.MuseumApp
import com.example.museumapp.databinding.ActivityAddAuthorBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import com.example.museumapp.ui.main.MainActivity

class AddAuthorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAuthorBinding
    private val viewModel: AuthorViewModel by viewModels {
        AuthorViewModelFactory((application as MuseumApp).authorRepository, (application as MuseumApp).exhibitRepository)
    }

    private var birthDateValue: String? = null
    private var deathDateValue: String? = null

    private val displayFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
    private val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAuthorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupDatePickers()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }

    private fun setupDatePickers() {
        val openBirthPicker = {
            val picker = buildDatePicker("Дата рождения", birthDateValue)
            picker.show(supportFragmentManager, "birth_date")
            picker.addOnPositiveButtonClickListener { selection ->
                birthDateValue = dbFormat.format(selection)
                binding.editTextBirthDate.setText(displayFormat.format(selection))
            }
        }
        binding.editTextBirthDate.setOnClickListener { openBirthPicker() }
        binding.layoutBirthDate.setEndIconOnClickListener { openBirthPicker() }

        val openDeathPicker = {
            val picker = buildDatePicker("Дата смерти", deathDateValue)
            picker.show(supportFragmentManager, "death_date")
            picker.addOnPositiveButtonClickListener { selection ->
                deathDateValue = dbFormat.format(selection)
                binding.editTextDeathDate.setText(displayFormat.format(selection))
            }
        }
        binding.editTextDeathDate.setOnClickListener { openDeathPicker() }
        binding.layoutDeathDate.setEndIconOnClickListener { openDeathPicker() }
    }

    private fun buildDatePicker(title: String, currentDbValue: String?): MaterialDatePicker<Long> {
        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())
            .build()
        val selection = currentDbValue?.let {
            runCatching { dbFormat.parse(it)?.time }.getOrNull()
        } ?: MaterialDatePicker.todayInUtcMilliseconds()

        return MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setSelection(selection)
            .setCalendarConstraints(constraints)
            .build()
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            val biography = binding.editTextBiography.text.toString().trim().takeIf { it.isNotBlank() }
            val photoUrl = binding.editTextPhotoUrl.text.toString().trim().takeIf { it.isNotBlank() }

            if (name.isEmpty()) {
                binding.editTextName.error = "Обязательное поле"
                binding.editTextName.requestFocus()
                return@setOnClickListener
            }

            viewModel.onEvent(
                AuthorEvent.SaveAuthor(
                    name = name,
                    biography = biography,
                    birthDate = birthDateValue,
                    deathDate = deathDateValue,
                    photoUrl = photoUrl
                )
            )
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                when (state) {
                    is AuthorState.Loading -> setLoading(true)
                    is AuthorState.Error -> {
                        showToast(state.message)
                        setLoading(false)
                    }
                    is AuthorState.AuthorAdded -> {
                        showToast("Автор добавлен")
                        finish()
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
        binding.editTextName.isEnabled = !isLoading
        binding.editTextBiography.isEnabled = !isLoading
        binding.editTextPhotoUrl.isEnabled = !isLoading
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
