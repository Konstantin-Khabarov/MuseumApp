package com.example.museumapp.ui.museums

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.museumapp.MuseumApp
import com.example.museumapp.data.auth.AuthManager
import com.example.museumapp.data.model.Museum
import com.example.museumapp.databinding.ActivityMuseumDetailBinding
import com.example.museumapp.ui.halls.HallAdapter
import com.example.museumapp.ui.halls.HallDetailActivity
import kotlinx.coroutines.launch
import com.example.museumapp.ui.main.MainActivity

class MuseumDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMuseumDetailBinding
    private val viewModel: MuseumViewModel by viewModels {
        MuseumViewModelFactory((application as MuseumApp).museumRepository, (application as MuseumApp).hallRepository)
    }

    private lateinit var currentMuseum: Museum
    private lateinit var hallAdapter: HallAdapter

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
        binding = ActivityMuseumDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        currentMuseum = Museum(
            id = intent.getIntExtra("museum_id", -1),
            name = intent.getStringExtra("museum_name") ?: "",
            address = intent.getStringExtra("museum_address") ?: "",
            city = intent.getStringExtra("museum_city") ?: "",
            country = intent.getStringExtra("museum_country"),
            website = intent.getStringExtra("museum_website")
        )

        displayMuseum(currentMuseum)
        setupHallsRecycler()
        observeViewModel()
        if (currentMuseum.id != -1) {
            viewModel.onEvent(MuseumEvent.LoadMuseumHalls(currentMuseum.id))
        }

        binding.btnEdit.setOnClickListener {
            if (!AuthManager.isAuthenticated()) {
                showToast("Для редактирования необходимо войти в систему")
                return@setOnClickListener
            }
            val intent = Intent(this, EditMuseumActivity::class.java).apply {
                putExtra("museum_id", currentMuseum.id)
                putExtra("museum_name", currentMuseum.name)
                putExtra("museum_city", currentMuseum.city)
                putExtra("museum_country", currentMuseum.country)
                putExtra("museum_address", currentMuseum.address)
                putExtra("museum_website", currentMuseum.website)
            }
            editLauncher.launch(intent)
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
            .setMessage("Удалить музей «${currentMuseum.name}»?\nТакже будут удалены все его залы.")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.onEvent(MuseumEvent.DeleteMuseum(currentMuseum.id))
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                when (state) {
                    is MuseumState.Loading -> setLoading(true)
                    is MuseumState.MuseumDeleted -> {
                        showToast("Музей удалён")
                        setResult(RESULT_OK)
                        finish()
                    }
                    is MuseumState.Error -> {
                        showToast(state.message)
                        setLoading(false)
                    }
                    is MuseumState.HallsLoading -> binding.progressBarHalls.visibility = View.VISIBLE
                    is MuseumState.MuseumHallsLoaded -> {
                        binding.progressBarHalls.visibility = View.GONE
                        if (state.halls.isEmpty()) binding.textNoHalls.visibility = View.VISIBLE
                        else hallAdapter.submitList(state.halls)
                    }
                    else -> setLoading(false)
                }
            }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnEdit.isEnabled = !isLoading
        binding.btnDelete.isEnabled = !isLoading
    }

    private fun displayMuseum(museum: Museum) {
        binding.textDetailName.text = museum.name
        val location = listOfNotNull(museum.city.takeIf { it.isNotBlank() }, museum.country)
            .joinToString(", ")
        binding.textDetailCity.text = location.ifBlank { "Город не указан" }
        binding.textDetailAddress.text = museum.address.ifBlank { "Адрес не указан" }

        val website = museum.website
        if (!website.isNullOrBlank()) {
            binding.textDetailWebsite.text = website
            binding.textDetailWebsite.setTextColor(android.graphics.Color.parseColor("#3F51B5"))
            binding.textDetailWebsite.isClickable = true
            binding.textDetailWebsite.setOnClickListener {
                val url = if (website.startsWith("http")) website else "https://$website"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        } else {
            binding.textDetailWebsite.text = "Сайт не указан"
            binding.textDetailWebsite.isClickable = false
            binding.textDetailWebsite.setTextColor(android.graphics.Color.parseColor("#999999"))
        }
    }

    private fun setupHallsRecycler() {
        hallAdapter = HallAdapter { hall ->
            startActivity(Intent(this, HallDetailActivity::class.java).apply {
                putExtra("hall_id", hall.hallId)
                putExtra("hall_museum_id", hall.museumId)
                putExtra("hall_name", hall.name)
                putExtra("hall_number", hall.hallNumber)
                putExtra("hall_museum_name", hall.museumName)
                putExtra("hall_description", hall.description)
                putExtra("hall_is_storage", hall.isStorage ?: false)
            })
        }
        binding.recyclerViewHalls.adapter = hallAdapter
        binding.recyclerViewHalls.layoutManager = LinearLayoutManager(this)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
