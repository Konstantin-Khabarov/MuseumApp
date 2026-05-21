package com.example.museumapp.ui.halls

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.museumapp.MuseumApp
import com.example.museumapp.data.auth.AuthManager
import com.example.museumapp.data.repository.HallItem
import com.example.museumapp.databinding.ActivityHallDetailBinding
import com.example.museumapp.ui.exhibits.ExhibitAdapter
import com.example.museumapp.ui.exhibits.ExhibitDetailActivity
import com.example.museumapp.ui.museums.MuseumDetailActivity
import kotlinx.coroutines.launch

class HallDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHallDetailBinding
    private val viewModel: HallViewModel by viewModels {
        HallViewModelFactory((application as MuseumApp).hallRepository, (application as MuseumApp).exhibitRepository, (application as MuseumApp).museumRepository)
    }

    private lateinit var currentHall: HallItem
    private lateinit var exhibitAdapter: ExhibitAdapter

    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) { setResult(RESULT_OK); finish() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHallDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        currentHall = HallItem(
            hallId = intent.getIntExtra("hall_id", -1),
            museumId = intent.getIntExtra("hall_museum_id", -1),
            hallNumber = intent.getStringExtra("hall_number"),
            name = intent.getStringExtra("hall_name"),
            museumName = intent.getStringExtra("hall_museum_name"),
            description = intent.getStringExtra("hall_description"),
            isStorage = intent.getBooleanExtra("hall_is_storage", false)
        )

        displayHall(currentHall)
        setupExhibitsRecycler()
        observeViewModel()

        if (currentHall.hallId != -1) {
            viewModel.onEvent(HallEvent.LoadHallExhibits(currentHall.hallId))
        }

        binding.btnEdit.setOnClickListener {
            if (!AuthManager.isAuthenticated()) { showToast("Для редактирования необходимо войти в систему"); return@setOnClickListener }
            editLauncher.launch(Intent(this, EditHallActivity::class.java).apply {
                putExtra("hall_id", currentHall.hallId)
                putExtra("hall_museum_id", currentHall.museumId)
                putExtra("hall_number", currentHall.hallNumber)
                putExtra("hall_name", currentHall.name)
                putExtra("hall_description", currentHall.description)
                putExtra("hall_is_storage", currentHall.isStorage ?: false)
            })
        }

        binding.btnDelete.setOnClickListener {
            if (!AuthManager.isAuthenticated()) { showToast("Для удаления необходимо войти в систему"); return@setOnClickListener }
            confirmAndDelete()
        }
    }

    private fun setupExhibitsRecycler() {
        exhibitAdapter = ExhibitAdapter { exhibit ->
            startActivity(Intent(this, ExhibitDetailActivity::class.java).apply {
                putExtra("exhibit_id", exhibit.id)
                putExtra("exhibit_author_id", exhibit.authorId)
                putExtra("exhibit_museum_id", exhibit.museumId)
                putExtra("exhibit_author_name", exhibit.authorName)
                putExtra("exhibit_museum_name", exhibit.museumName)
            })
        }
        binding.recyclerViewExhibits.adapter = exhibitAdapter
        binding.recyclerViewExhibits.layoutManager = LinearLayoutManager(this)
    }

    private fun confirmAndDelete() {
        AlertDialog.Builder(this)
            .setTitle("Подтверждение")
            .setMessage("Удалить зал «${currentHall.name ?: "№${currentHall.hallNumber}"}»?")
            .setPositiveButton("Удалить") { _, _ -> viewModel.onEvent(HallEvent.DeleteHall(currentHall.hallId)) }
            .setNegativeButton("Отмена", null).show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.navigationEvent.collect { event ->
                if (event is HallNavigationEvent.ToMuseum) {
                    val m = event.museum
                    startActivity(Intent(this@HallDetailActivity, MuseumDetailActivity::class.java).apply {
                        putExtra("museum_id", m.id); putExtra("museum_name", m.name)
                        putExtra("museum_city", m.city); putExtra("museum_country", m.country)
                        putExtra("museum_address", m.address); putExtra("museum_website", m.website)
                    })
                }
            }
        }
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is HallState.Loading -> setLoading(true)
                    is HallState.HallDeleted -> { showToast("Зал удалён"); setResult(RESULT_OK); finish() }
                    is HallState.Error -> { showToast(state.message); setLoading(false) }
                    is HallState.ExhibitsLoading -> binding.progressBarExhibits.visibility = View.VISIBLE
                    is HallState.HallExhibitsLoaded -> {
                        binding.progressBarExhibits.visibility = View.GONE
                        if (state.exhibits.isEmpty()) binding.textNoExhibits.visibility = View.VISIBLE
                        else exhibitAdapter.submitList(state.exhibits)
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

    private fun displayHall(hall: HallItem) {
        binding.textDetailName.text = hall.name?.takeIf { it.isNotBlank() } ?: "Без названия"
        binding.textDetailHallNumber.text = hall.hallNumber?.let { "Зал №$it" } ?: "Номер не указан"
        binding.textDetailDescription.text = hall.description?.takeIf { it.isNotBlank() } ?: "Описание отсутствует"
        binding.textDetailIsStorage.text = if (hall.isStorage == true) "Тип: хранилище" else "Тип: выставочный зал"

        val museumName = hall.museumName
        if (!museumName.isNullOrBlank() && hall.museumId != -1) {
            val spannable = SpannableString(museumName)
            spannable.setSpan(UnderlineSpan(), 0, museumName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.textDetailMuseum.text = spannable
            binding.textDetailMuseum.setTextColor(Color.parseColor("#3F51B5"))
            binding.textDetailMuseum.setOnClickListener {
                viewModel.onEvent(HallEvent.FetchMuseumForNav(hall.museumId))
            }
        } else {
            binding.textDetailMuseum.text = museumName ?: "Музей не указан"
            binding.textDetailMuseum.setTextColor(Color.parseColor("#666666"))
            binding.textDetailMuseum.setOnClickListener(null)
        }
    }

    private fun showToast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
