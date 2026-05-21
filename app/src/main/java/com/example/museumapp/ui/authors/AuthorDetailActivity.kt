package com.example.museumapp.ui.authors

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.RoundedCornersTransformation
import com.example.museumapp.MuseumApp
import com.example.museumapp.R
import com.example.museumapp.data.auth.AuthManager
import com.example.museumapp.data.model.Author
import com.example.museumapp.databinding.ActivityAuthorDetailBinding
import kotlinx.coroutines.launch

class AuthorDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthorDetailBinding
    private val viewModel: AuthorViewModel by viewModels {
        AuthorViewModelFactory((application as MuseumApp).authorRepository)
    }

    private lateinit var currentAuthor: Author

    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Перезагружаем список и закрываем детали — пользователь вернётся к свежему списку
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { finish() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthorDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()

        currentAuthor = Author(
            id = intent.getIntExtra("author_id", -1),
            name = intent.getStringExtra("author_name") ?: "",
            biography = intent.getStringExtra("author_bio"),
            birthDate = intent.getStringExtra("author_birth_date"),
            deathDate = intent.getStringExtra("author_death_date"),
            photoUrl = intent.getStringExtra("author_photo_url")
        )

        displayAuthor(currentAuthor)
        observeViewModel()

        binding.btnEdit.setOnClickListener {
            if (!AuthManager.isAuthenticated()) {
                showToast("Для редактирования необходимо войти в систему")
                return@setOnClickListener
            }
            val editIntent = Intent(this, EditAuthorActivity::class.java).apply {
                putExtra("author_id", currentAuthor.id)
                putExtra("author_name", currentAuthor.name)
                putExtra("author_bio", currentAuthor.biography)
                putExtra("author_birth_date", currentAuthor.birthDate)
                putExtra("author_death_date", currentAuthor.deathDate)
                putExtra("author_photo_url", currentAuthor.photoUrl)
            }
            editLauncher.launch(editIntent)
        }

        binding.btnDelete.setOnClickListener {
            if (!AuthManager.isAuthenticated()) {
                showToast("Для удаления необходимо войти в систему")
                return@setOnClickListener
            }
            confirmAndDelete()
        }
    }

    private fun confirmAndDelete() {
        AlertDialog.Builder(this)
            .setTitle("Подтверждение")
            .setMessage("Вы действительно хотите удалить этого автора?")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.onEvent(AuthorEvent.DeleteAuthor(currentAuthor.id))
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is AuthorState.Loading -> setLoading(true)
                    is AuthorState.AuthorDeleted -> {
                        showToast("Автор удалён")
                        setResult(RESULT_OK)
                        finish()
                    }
                    is AuthorState.Error -> {
                        showToast(state.message)
                        setLoading(false)
                    }
                    else -> setLoading(false)
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnEdit.isEnabled = !isLoading
        binding.btnDelete.isEnabled = !isLoading
    }

    private fun displayAuthor(author: Author) {
        binding.textDetailName.text = author.name
        binding.textDetailBio.text = author.biography ?: "Биография отсутствует"
        val birthDeathText = if (author.birthDate != null) {
            if (author.deathDate != null) {
                "${author.birthDate} — ${author.deathDate}"
            } else {
                "Родился: ${author.birthDate}"
            }
        } else {
            "Годы жизни неизвестны"
        }
        binding.textDetailBirthDeath.text = birthDeathText

        if (!author.photoUrl.isNullOrBlank()) {
            binding.imageViewAuthor.scaleType = ImageView.ScaleType.CENTER_CROP
            binding.imageViewAuthor.load(author.photoUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_no_image)
                error(R.drawable.ic_no_image)
                transformations(RoundedCornersTransformation(12f))
                listener(
                    onError = { _, _ ->
                        binding.imageViewAuthor.scaleType = ImageView.ScaleType.CENTER
                    }
                )
            }
        } else {
            binding.imageViewAuthor.scaleType = ImageView.ScaleType.CENTER
            binding.imageViewAuthor.setImageResource(R.drawable.ic_no_image)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}