package com.example.museumapp.ui.museums

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.museumapp.MuseumApp
import com.example.museumapp.data.auth.AuthManager
import com.example.museumapp.databinding.ActivityEditMuseumBinding
import kotlinx.coroutines.launch

class EditMuseumActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditMuseumBinding
    private val viewModel: MuseumViewModel by viewModels {
        MuseumViewModelFactory((application as MuseumApp).museumRepository, (application as MuseumApp).hallRepository)
    }

    private var museumId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditMuseumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        museumId = intent.getIntExtra("museum_id", -1)
        if (museumId == -1) {
            showToast("Ошибка: не передан ID музея")
            finish()
            return
        }

        fillForm()
        binding.btnBack.setOnClickListener { finish() }
        setupListeners()
        observeViewModel()
    }

    private fun fillForm() {
        binding.editTextName.setText(intent.getStringExtra("museum_name") ?: "")
        binding.editTextCity.setText(intent.getStringExtra("museum_city") ?: "")
        binding.editTextCountry.setText(intent.getStringExtra("museum_country") ?: "")
        binding.editTextAddress.setText(intent.getStringExtra("museum_address") ?: "")
        binding.editTextWebsite.setText(intent.getStringExtra("museum_website") ?: "")
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            if (!AuthManager.isAuthenticated()) {
                showToast("Для редактирования необходимо войти в систему")
                return@setOnClickListener
            }
            val name = binding.editTextName.text.toString().trim()
            if (name.isEmpty()) {
                binding.editTextName.error = "Обязательное поле"
                binding.editTextName.requestFocus()
                return@setOnClickListener
            }
            viewModel.onEvent(
                MuseumEvent.UpdateMuseum(
                    museumId = museumId,
                    name = name,
                    address = binding.editTextAddress.text.toString().trim(),
                    city = binding.editTextCity.text.toString().trim(),
                    country = binding.editTextCountry.text.toString().trim().takeIf { it.isNotBlank() },
                    website = binding.editTextWebsite.text.toString().trim().takeIf { it.isNotBlank() }
                )
            )
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is MuseumState.Loading -> setLoading(true)
                    is MuseumState.MuseumUpdated -> {
                        showToast("Музей обновлён")
                        setResult(RESULT_OK)
                        finish()
                    }
                    is MuseumState.Error -> {
                        showToast(state.message)
                        setLoading(false)
                    }
                    else -> {}
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !isLoading
        binding.editTextName.isEnabled = !isLoading
        binding.editTextCity.isEnabled = !isLoading
        binding.editTextCountry.isEnabled = !isLoading
        binding.editTextAddress.isEnabled = !isLoading
        binding.editTextWebsite.isEnabled = !isLoading
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
