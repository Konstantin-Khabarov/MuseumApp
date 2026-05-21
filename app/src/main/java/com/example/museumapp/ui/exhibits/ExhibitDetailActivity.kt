package com.example.museumapp.ui.exhibits

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.museumapp.ui.authors.AuthorDetailActivity
import com.example.museumapp.ui.halls.HallDetailActivity
import com.example.museumapp.ui.museums.MuseumDetailActivity
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
        ExhibitViewModelFactory((application as MuseumApp).exhibitRepository, (application as MuseumApp).authorRepository, (application as MuseumApp).museumRepository, (application as MuseumApp).hallRepository)
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
                    is ExhibitState.ExhibitDetailsLoaded -> {
                        binding.progressBar.visibility = View.GONE
                        updateUIWithExhibit(state.exhibit)
                    }
                    is ExhibitState.Error -> {
                        showToast(state.message)
                        binding.btnDelete.isEnabled = true
                        binding.progressBar.visibility = View.GONE
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
        lifecycleScope.launch {
            viewModel.navigationEvent.collect { event ->
                when (event) {
                    is ExhibitNavigationEvent.ToAuthor -> {
                        val a = event.author
                        startActivity(Intent(this@ExhibitDetailActivity, AuthorDetailActivity::class.java).apply {
                            putExtra("author_id", a.id); putExtra("author_name", a.name)
                            putExtra("author_bio", a.biography); putExtra("author_birth_date", a.birthDate)
                            putExtra("author_death_date", a.deathDate); putExtra("author_photo_url", a.photoUrl)
                        })
                    }
                    is ExhibitNavigationEvent.ToMuseum -> {
                        val m = event.museum
                        startActivity(Intent(this@ExhibitDetailActivity, MuseumDetailActivity::class.java).apply {
                            putExtra("museum_id", m.id); putExtra("museum_name", m.name)
                            putExtra("museum_city", m.city); putExtra("museum_country", m.country)
                            putExtra("museum_address", m.address); putExtra("museum_website", m.website)
                        })
                    }
                    is ExhibitNavigationEvent.ToHall -> {
                        val h = event.hall
                        startActivity(Intent(this@ExhibitDetailActivity, HallDetailActivity::class.java).apply {
                            putExtra("hall_id", h.hallId); putExtra("hall_museum_id", h.museumId)
                            putExtra("hall_name", h.name); putExtra("hall_number", h.hallNumber)
                            putExtra("hall_museum_name", h.museumName); putExtra("hall_description", h.description)
                            putExtra("hall_is_storage", h.isStorage ?: false)
                        })
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
        val authorName = exhibit.authorName
        if (!authorName.isNullOrBlank() && exhibit.authorId != null) {
            val spannable = SpannableString(authorName)
            spannable.setSpan(UnderlineSpan(), 0, authorName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.textDetailAuthor.text = spannable
            binding.textDetailAuthor.setTextColor(Color.parseColor("#3F51B5"))
            binding.textDetailAuthor.setOnClickListener {
                viewModel.onEvent(ExhibitEvent.FetchAuthorForNav(exhibit.authorId))
            }
        } else {
            binding.textDetailAuthor.text = "Автор не указан"
            binding.textDetailAuthor.setTextColor(Color.parseColor("#666666"))
            binding.textDetailAuthor.setOnClickListener(null)
        }
        // Зал
        val hallText = exhibit.hallNumber?.let { "Зал №$it" } ?: "Зал не указан"
        if (exhibit.hallId != null) {
            val spannable = SpannableString(hallText)
            spannable.setSpan(UnderlineSpan(), 0, hallText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.textDetailHall.text = spannable
            binding.textDetailHall.setTextColor(Color.parseColor("#3F51B5"))
            binding.textDetailHall.setOnClickListener {
                viewModel.onEvent(ExhibitEvent.FetchHallForNav(exhibit.hallId))
            }
        } else {
            binding.textDetailHall.text = hallText
            binding.textDetailHall.setTextColor(Color.parseColor("#666666"))
            binding.textDetailHall.setOnClickListener(null)
        }

        // Музей
        val museumName = exhibit.museumName
        if (!museumName.isNullOrBlank() && exhibit.museumId != null) {
            val spannable = SpannableString(museumName)
            spannable.setSpan(UnderlineSpan(), 0, museumName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.textDetailMuseum.text = spannable
            binding.textDetailMuseum.setTextColor(Color.parseColor("#3F51B5"))
            binding.textDetailMuseum.setOnClickListener {
                viewModel.onEvent(ExhibitEvent.FetchMuseumForNav(exhibit.museumId))
            }
        } else {
            binding.textDetailMuseum.text = museumName ?: "Музей не указан"
            binding.textDetailMuseum.setTextColor(Color.parseColor("#666666"))
            binding.textDetailMuseum.setOnClickListener(null)
        }

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