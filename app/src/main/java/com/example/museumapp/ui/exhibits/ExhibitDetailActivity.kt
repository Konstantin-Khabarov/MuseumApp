package com.example.museumapp.ui.exhibits

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import com.example.museumapp.data.model.Exhibit
import com.example.museumapp.databinding.ActivityExhibitDetailBinding
import kotlinx.coroutines.launch

class ExhibitDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExhibitDetailBinding
    private val viewModel: ExhibitViewModel by viewModels {
        ExhibitViewModelFactory((application as MuseumApp).exhibitRepository)
    }

    private var currentExhibitId: Int = -1

    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExhibitDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*val exhibit = Exhibit(
            id = intent.getIntExtra("exhibit_id", -1),
            title = intent.getStringExtra("exhibit_title") ?: "",
            description = intent.getStringExtra("exhibit_description") ?: "",
            creationYear = intent.getIntExtra("exhibit_creation_year", 0),
            authorId = intent.getIntExtra("exhibit_author_id", -1).takeIf { it != -1 },
            museumId = intent.getIntExtra("exhibit_museum_id", -1).takeIf { it != -1 },
            authorName = intent.getStringExtra("exhibit_author_name"),
            museumName = intent.getStringExtra("exhibit_museum_name"),
            //imageUrl = intent.getStringExtra("exhibit_image_url")
        )*/
        currentExhibitId = intent.getIntExtra("exhibit_id", -1)
        viewModel.loadExhibitDetails(currentExhibitId)

        //currentExhibitId = exhibit.id

        //displayExhibit(exhibit)
        setupToolbar()
        setupListeners()
        viewModel.loadExhibitDetails(currentExhibitId)
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        // ✏️ Редактирование (заглушка)
        binding.btnEdit.setOnClickListener {
            if (!AuthManager.isAuthenticated()) {
                showToast("Для редактирования необходимо войти в систему")
                return@setOnClickListener
            }

            /*val intent = Intent(this, EditExhibitActivity::class.java).apply {
                putExtra("exhibit_id", currentExhibitId)
                putExtra("exhibit_title", intent.getStringExtra("exhibit_title") ?: "")
                putExtra("exhibit_description", intent.getStringExtra("exhibit_description") ?: "")
                putExtra("exhibit_creation_year", intent.getIntExtra("exhibit_creation_year", 0))
                putExtra("exhibit_hall_id", intent.getIntExtra("exhibit_hall_id", -1))
                putExtra("exhibit_museum_id", intent.getIntExtra("exhibit_museum_id", -1))
            }
            startActivity(intent)*/
            val intent = Intent(this, EditExhibitActivity::class.java)
            intent.putExtra("exhibit_id", currentExhibitId)
            editLauncher.launch(intent)
        }

        // 🗑️ Удаление с подтверждением
        binding.btnDelete.setOnClickListener {
            // 🔥 Проверка авторизации
            if (!AuthManager.isAuthenticated()) {
                showToast("Для удаления необходимо войти в систему")
                return@setOnClickListener
            }

            confirmAndDeleteExhibit()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is ExhibitState.Loading -> {
                        // 🔥 Блокируем интерфейс во время удаления
                        //binding.btnDelete.isEnabled = false
                        //binding.progressBar.visibility = View.VISIBLE
                    }
                    is ExhibitState.ExhibitDetailsLoaded -> {
                        binding.progressBar.visibility = View.GONE
                        updateUIWithExhibit(state.exhibit)
                    }
                    is ExhibitState.Success -> {
                        binding.btnDelete.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                    }
                    is ExhibitState.Error -> {
                        showToast(state.message)
                        binding.btnDelete.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                    }
                    is ExhibitState.ShowMessage -> {
                        showToast(state.message)
                    }
                    is ExhibitState.NavigateBack -> {
                        showToast("Экспонат удалён")
                        setResult(RESULT_OK)
                        finish()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun updateUIWithExhibit(exhibit: Exhibit) {
        binding.textDetailName.text = exhibit.title
        binding.textDetailDescription.text = exhibit.description
        binding.textDetailDate.text = "Год: ${exhibit.creationYear}"
        binding.textDetailAuthor.text = exhibit.authorName ?: "Автор не указан"
        binding.textDetailMuseum.text = exhibit.museumName ?: "Музей не указан"

        android.util.Log.d("IMG_DEBUG", "updateUI: exhibit id=${exhibit.id} imageUrl='${exhibit.imageUrl}'")

        if (!exhibit.imageUrl.isNullOrBlank()) {
            android.util.Log.d("IMG_DEBUG", "Coil: starting load for url='${exhibit.imageUrl}'")
            binding.imageViewExhibit.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            binding.imageViewExhibit.load(exhibit.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_no_image)
                error(R.drawable.ic_no_image)
                transformations(RoundedCornersTransformation(12f))
                listener(
                    onStart = {
                        android.util.Log.d("IMG_DEBUG", "Coil: onStart")
                    },
                    onSuccess = { _, _ ->
                        android.util.Log.d("IMG_DEBUG", "Coil: onSuccess — image loaded!")
                    },
                    onError = { _, result ->
                        android.util.Log.e("IMG_DEBUG", "Coil: onError — ${result.throwable}")
                        binding.imageViewExhibit.scaleType = android.widget.ImageView.ScaleType.CENTER
                    }
                )
            }
        } else {
            android.util.Log.d("IMG_DEBUG", "imageUrl is null/blank — showing placeholder")
            binding.imageViewExhibit.scaleType = android.widget.ImageView.ScaleType.CENTER
            binding.imageViewExhibit.setImageResource(R.drawable.ic_no_image)
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

    private fun confirmAndDeleteExhibit() {
        // Простое подтверждение удаления
        AlertDialog.Builder(this)
            .setTitle("Подтверждение")
            .setMessage("Вы действительно хотите удалить этот экспонат ?")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.onEvent(ExhibitEvent.DeleteExhibit(currentExhibitId))
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}