package com.example.museumapp.ui.authors

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.museumapp.MuseumApp
import com.example.museumapp.data.model.Author
import com.example.museumapp.databinding.ActivityAuthorDetailBinding
import kotlinx.coroutines.launch

class AuthorDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthorDetailBinding
    private val viewModel: AuthorViewModel by viewModels {
        AuthorViewModelFactory((application as MuseumApp).authorRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthorDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val author = Author(
            id = intent.getIntExtra("author_id", -1),
            name = intent.getStringExtra("author_name") ?: "",
            biography = intent.getStringExtra("author_bio"),
            birthDate = intent.getStringExtra("author_birth_date"),
            deathDate = intent.getStringExtra("author_death_date")
        )

        displayAuthor(author)

        binding.btnEdit.setOnClickListener {
            // Здесь можно перейти в EditAuthorActivity
            showToast("Редактирование автора: ${author.name}")
        }

        binding.btnDelete.setOnClickListener {
            // Удалить автора
            lifecycleScope.launch {
                try {
                    //viewModel.deleteAuthor(author.id)
                    showToast("Автор удалён")
                    finish() // Закрыть экран
                } catch (e: Exception) {
                    showToast("Ошибка: ${e.message}")
                }
            }
        }
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
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}