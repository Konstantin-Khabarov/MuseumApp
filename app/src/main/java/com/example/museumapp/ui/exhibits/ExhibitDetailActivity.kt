package com.example.museumapp.ui.exhibits

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.museumapp.MuseumApp
import com.example.museumapp.data.model.Exhibit
import com.example.museumapp.databinding.ActivityExhibitDetailBinding
import kotlinx.coroutines.launch

class ExhibitDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExhibitDetailBinding
    private val viewModel: ExhibitViewModel by viewModels {
        ExhibitViewModelFactory((application as MuseumApp).exhibitRepository)
    }

    private var currentExhibitId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExhibitDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val exhibit = Exhibit(
            id = intent.getIntExtra("exhibit_id", -1),
            title = intent.getStringExtra("exhibit_title") ?: "",
            description = intent.getStringExtra("exhibit_description") ?: "",
            creationYear = intent.getIntExtra("exhibit_creation_year", 0),
            authorId = intent.getIntExtra("exhibit_author_id", -1).takeIf { it != -1 },
            museumId = intent.getIntExtra("exhibit_museum_id", -1).takeIf { it != -1 },
            authorName = intent.getStringExtra("exhibit_author_name"),
            museumName = intent.getStringExtra("exhibit_museum_name"),
            //imageUrl = intent.getStringExtra("exhibit_image_url")
        )

        currentExhibitId = exhibit.id

        displayExhibit(exhibit)

        // Кнопка назад
        binding.btnBack.setOnClickListener {
            finish() // Или: onBackPressedDispatcher.onBackPressed()
        }

        binding.btnEdit.setOnClickListener {
            // Здесь можно перейти в EditExhibitActivity
            showToast("Редактирование экспоната: ${exhibit.title}")
        }

        binding.btnDelete.setOnClickListener {
            confirmAndDeleteExhibit(exhibit)
        }
    }

    private fun displayExhibit(exhibit: Exhibit) {
        // Название
        binding.textDetailName.text = exhibit.title.ifEmpty { "Без названия" }

        // Описание
        binding.textDetailDescription.text =
            exhibit.description.ifEmpty { "Описание отсутствует" }

        // Дата создания
        binding.textDetailDate.text = "Год создания: ${exhibit.creationYear}"

        // Автор: показываем имя, если есть, иначе ID
        val authorText = when {
            !exhibit.authorName.isNullOrBlank() -> exhibit.authorName
            else -> "Автор не указан"
        }
        binding.textDetailAuthor.text = authorText

        // Музей: показываем название, если есть, иначе ID
        val museumText = when {
            !exhibit.museumName.isNullOrBlank() -> exhibit.museumName
            else -> "Музей не указан"
        }
        binding.textDetailMuseum.text = museumText
    }

    private fun confirmAndDeleteExhibit(exhibit: Exhibit) {
        // Простое подтверждение удаления
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Подтверждение")
            .setMessage("Удалить экспонат \"${exhibit.title}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                performDelete(exhibit.id)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    private fun performDelete(exhibitId: Int) {
        lifecycleScope.launch {
            try {
                // 🔥 Вызов удаления через ViewModel
                // viewModel.deleteExhibit(exhibitId)

                // Для теста покажем сообщение
                showToast("Экспонат удалён")
                finish()
            } catch (e: Exception) {
                showToast("Ошибка: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}