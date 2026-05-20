package com.example.museumapp.ui.museums

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.museumapp.MuseumApp
import com.example.museumapp.data.model.Museum
import com.example.museumapp.databinding.ActivityMuseumDetailBinding
import kotlinx.coroutines.launch

class MuseumDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMuseumDetailBinding
    private val viewModel: MuseumViewModel by viewModels {
        MuseumViewModelFactory((application as MuseumApp).museumRepository)
    }
    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMuseumDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()

        val museum = Museum(
            id = intent.getIntExtra("museum_id", -1),
            name = intent.getStringExtra("museum_name") ?: "",
            address = intent.getStringExtra("museum_address") ?: "",
            city = intent.getStringExtra("museum_city") ?: ""
        )

        displayMuseum(museum)

        binding.btnEdit.setOnClickListener {
            // Здесь можно перейти в EditMuseumActivity
            showToast("Редактирование музея: ${museum.name}")
        }

        binding.btnDelete.setOnClickListener {
            lifecycleScope.launch {
                try {
                    //viewModel.deleteMuseum(Museum.id)
                    showToast("Музей удалён")
                    finish() // Закрыть экран
                } catch (e: Exception) {
                    showToast("Ошибка: ${e.message}")
                }
            }
        }
    }

    private fun displayMuseum(museum: Museum) {
        binding.textDetailName.text = museum.name
        binding.textDetailCity.text = museum.city
        binding.textDetailAddress.text = museum.address
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}