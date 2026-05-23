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
import com.example.museumapp.data.auth.AuthManager
import com.example.museumapp.databinding.ActivityEditAuthorBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import com.example.museumapp.ui.main.MainActivity

class EditAuthorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditAuthorBinding
    private val viewModel: AuthorViewModel by viewModels {
        AuthorViewModelFactory((application as MuseumApp).authorRepository, (application as MuseumApp).exhibitRepository)
    }

    private var authorId: Int = -1
    private var birthDateValue: String? = null
    private var deathDateValue: String? = null

    private val displayFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
    private val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAuthorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authorId = intent.getIntExtra("author_id", -1)
        if (authorId == -1) {
            showToast("Ошибка: не передан ID автора")
            finish()
            return
        }

        fillForm()
        setupToolbar()
        setupDatePickers()
        setupListeners()
        observeViewModel()
    }

    private fun fillForm() {
        binding.editTextName.setText(intent.getStringExtra("author_name") ?: "")
        binding.editTextBiography.setText(intent.getStringExtra("author_bio") ?: "")
        binding.editTextPhotoUrl.setText(intent.getStringExtra("author_photo_url") ?: "")

        intent.getStringExtra("author_birth_date")?.takeIf { it.isNotBlank() }?.let { raw ->
            birthDateValue = raw
            runCatching { dbFormat.parse(raw)?.let { binding.editTextBirthDate.setText(displayFormat.format(it)) } }
        }
        intent.getStringExtra("author_death_date")?.takeIf { it.isNotBlank() }?.let { raw ->
            deathDateValue = raw
            runCatching { dbFormat.parse(raw)?.let { binding.editTextDeathDate.setText(displayFormat.format(it)) } }
        }
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
            if (!AuthManager.isAuthenticated()) {
                showToast("Для редактирования необходимо войти в систему")
                return@setOnClickListener
            }
            val name = binding.editTextName.text.toString().trim()
            if (name.isEmpty()) {
                binding.editTextName.error = "Обязательное поле"
                binding.editTextName.requestFocus()
                return@setOnClickListener
            }

            viewModel.onEvent(
                AuthorEvent.UpdateAuthor(
                    authorId = authorId,
                    name = name,
                    biography = binding.editTextBiography.text.toString().trim().takeIf { it.isNotBlank() },
                    birthDate = birthDateValue,
                    deathDate = deathDateValue,
                    photoUrl = binding.editTextPhotoUrl.text.toString().trim().takeIf { it.isNotBlank() }
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
                    is AuthorState.AuthorUpdated -> {
                        showToast("Автор обновлён")
                        setResult(RESULT_OK)
                        finish()
                    }
                    is AuthorState.Error -> {
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
        binding.editTextName.isEnabled = !isLoading
        binding.editTextBiography.isEnabled = !isLoading
        binding.editTextPhotoUrl.isEnabled = !isLoading
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
