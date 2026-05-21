package com.example.museumapp.ui.halls

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.museumapp.MuseumApp
import com.example.museumapp.data.auth.AuthManager
import com.example.museumapp.data.repository.HallItem
import com.example.museumapp.databinding.ActivityHallDetailBinding
import kotlinx.coroutines.launch

class HallDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHallDetailBinding
    private val viewModel: HallViewModel by viewModels {
        HallViewModelFactory((application as MuseumApp).hallRepository)
    }

    private lateinit var currentHall: HallItem

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
        observeViewModel()

        binding.btnEdit.setOnClickListener {
            if (!AuthManager.isAuthenticated()) {
                showToast("Для редактирования необходимо войти в систему")
                return@setOnClickListener
            }
            val intent = Intent(this, EditHallActivity::class.java).apply {
                putExtra("hall_id", currentHall.hallId)
                putExtra("hall_museum_id", currentHall.museumId)
                putExtra("hall_number", currentHall.hallNumber)
                putExtra("hall_name", currentHall.name)
                putExtra("hall_description", currentHall.description)
                putExtra("hall_is_storage", currentHall.isStorage ?: false)
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
            .setMessage("Удалить зал «${currentHall.name ?: "№${currentHall.hallNumber}"}»?")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.onEvent(HallEvent.DeleteHall(currentHall.hallId))
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is HallState.Loading -> setLoading(true)
                    is HallState.HallDeleted -> {
                        showToast("Зал удалён")
                        setResult(RESULT_OK)
                        finish()
                    }
                    is HallState.Error -> {
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

    private fun displayHall(hall: HallItem) {
        binding.textDetailName.text = hall.name?.takeIf { it.isNotBlank() } ?: "Без названия"
        binding.textDetailHallNumber.text = hall.hallNumber?.let { "Зал №$it" } ?: "Номер не указан"
        binding.textDetailMuseum.text = hall.museumName ?: "Музей не указан"
        binding.textDetailDescription.text = hall.description?.takeIf { it.isNotBlank() } ?: "Описание отсутствует"
        binding.textDetailIsStorage.text = if (hall.isStorage == true) "Тип: хранилище" else "Тип: выставочный зал"
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
