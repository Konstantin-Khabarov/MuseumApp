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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
import com.example.museumapp.ui.main.MainActivity

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

        currentExhibitId = intent.getIntExtra("exhibit_id", -1)
        viewModel.loadExhibitDetails(currentExhibitId)

        setupToolbar()
        setupListeners()
        viewModel.loadExhibitDetails(currentExhibitId)
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }

    private fun setupListeners() {

        binding.btnEdit.setOnClickListener {
            if (!AuthManager.isAuthenticated()) {
                showToast("Для редактирования необходимо войти в систему")
                return@setOnClickListener
            }

            val intent = Intent(this, EditExhibitActivity::class.java)
            intent.putExtra("exhibit_id", currentExhibitId)
            editLauncher.launch(intent)
        }

        binding.btnDelete.setOnClickListener {

            if (!AuthManager.isAuthenticated()) {
                showToast("Для удаления необходимо войти в систему")
                return@setOnClickListener
            }

            confirmAndDeleteExhibit()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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

        if (!exhibit.imageUrl.isNullOrBlank()) {
            binding.imageViewExhibit.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            binding.imageViewExhibit.load(exhibit.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_no_image)
                error(R.drawable.ic_no_image)
                transformations(RoundedCornersTransformation(12f))
                listener(
                    onStart = {
                    },
                    onSuccess = { _, _ ->
                    },
                    onError = { _, result ->
                        binding.imageViewExhibit.scaleType = android.widget.ImageView.ScaleType.CENTER
                    }
                )
            }
        } else {
            binding.imageViewExhibit.scaleType = android.widget.ImageView.ScaleType.CENTER
            binding.imageViewExhibit.setImageResource(R.drawable.ic_no_image)
        }
    }

    private fun confirmAndDeleteExhibit() {

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
