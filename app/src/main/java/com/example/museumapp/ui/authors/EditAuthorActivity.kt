package com.example.museumapp.ui.authors

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.museumapp.MuseumApp
import com.example.museumapp.data.auth.AuthManager
import com.example.museumapp.databinding.ActivityEditAuthorBinding
import kotlinx.coroutines.launch

class EditAuthorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditAuthorBinding
    private val viewModel: AuthorViewModel by viewModels {
        AuthorViewModelFactory((application as MuseumApp).authorRepository, (application as MuseumApp).exhibitRepository)
    }

    private var authorId: Int = -1

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
        setupListeners()
        observeViewModel()
    }

    private fun fillForm() {
        binding.editTextName.setText(intent.getStringExtra("author_name") ?: "")
        binding.editTextBiography.setText(intent.getStringExtra("author_bio") ?: "")
        binding.editTextBirthDate.setText(intent.getStringExtra("author_birth_date") ?: "")
        binding.editTextDeathDate.setText(intent.getStringExtra("author_death_date") ?: "")
        binding.editTextPhotoUrl.setText(intent.getStringExtra("author_photo_url") ?: "")
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { finish() }
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
                    birthDate = binding.editTextBirthDate.text.toString().trim().takeIf { it.isNotBlank() },
                    deathDate = binding.editTextDeathDate.text.toString().trim().takeIf { it.isNotBlank() },
                    photoUrl = binding.editTextPhotoUrl.text.toString().trim().takeIf { it.isNotBlank() }
                )
            )
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
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

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !isLoading
        binding.editTextName.isEnabled = !isLoading
        binding.editTextBiography.isEnabled = !isLoading
        binding.editTextBirthDate.isEnabled = !isLoading
        binding.editTextDeathDate.isEnabled = !isLoading
        binding.editTextPhotoUrl.isEnabled = !isLoading
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
