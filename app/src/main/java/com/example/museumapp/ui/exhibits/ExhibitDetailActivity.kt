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
            //imageUrl = intent.getStringExtra("exhibit_image_url")
        )

        displayExhibit(exhibit)

        // Кнопка назад
        /*binding.btnBack.setOnClickListener {
            finish()
        }*/

        binding.btnEdit.setOnClickListener {
            // Здесь можно перейти в EditExhibitActivity
            showToast("Редактирование экспоната: ${exhibit.title}")
        }

        binding.btnDelete.setOnClickListener {
            // Удалить экспонат
            lifecycleScope.launch {
                try {
                    // viewModel.deleteExhibit(exhibit.id)
                    showToast("Экспонат удалён")
                    finish() // Закрыть экран
                } catch (e: Exception) {
                    showToast("Ошибка: ${e.message}")
                }
            }
        }
    }

    private fun displayExhibit(exhibit: Exhibit) {
        binding.textDetailName.text = exhibit.title
        binding.textDetailDescription.text = exhibit.description.ifEmpty { "Описание отсутствует" }
        binding.textDetailDate.text = "Дата создания: ${exhibit.creationYear}"

        // Отображение информации об авторе
        val authorText = if (exhibit.authorId != null) {
            "ID автора: ${exhibit.authorId}"
        } else {
            "Автор не указан"
        }
        binding.textDetailAuthor.text = authorText

        // Отображение информации о музее
        val museumText = if (exhibit.museumId != null) {
            "ID музея: ${exhibit.museumId}"
        } else {
            "Музей не указан"
        }
        binding.textDetailMuseum.text = museumText
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}