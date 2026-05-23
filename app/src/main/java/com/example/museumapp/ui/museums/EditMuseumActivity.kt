package com.example.museumapp.ui.museums

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.museumapp.MuseumApp
import com.example.museumapp.data.auth.AuthManager
import com.example.museumapp.databinding.ActivityEditMuseumBinding
import kotlinx.coroutines.launch
import com.example.museumapp.ui.main.MainActivity

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
        binding.btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
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
            val city = binding.editTextCity.text.toString().trim()
            val country = binding.editTextCountry.text.toString().trim()
            val address = binding.editTextAddress.text.toString().trim()

            var hasError = false
            if (name.isEmpty()) {
                binding.editTextName.error = "Обязательное поле"
                if (!hasError) { binding.editTextName.requestFocus(); hasError = true }
            }
            if (city.isEmpty()) {
                binding.editTextCity.error = "Обязательное поле"
                if (!hasError) { binding.editTextCity.requestFocus(); hasError = true }
            }
            if (country.isEmpty()) {
                binding.editTextCountry.error = "Обязательное поле"
                if (!hasError) { binding.editTextCountry.requestFocus(); hasError = true }
            }
            if (address.isEmpty()) {
                binding.editTextAddress.error = "Обязательное поле"
                if (!hasError) { binding.editTextAddress.requestFocus(); hasError = true }
            }
            if (hasError) {
                showToast("Заполните все обязательные поля (*)")
                return@setOnClickListener
            }

            viewModel.onEvent(
                MuseumEvent.UpdateMuseum(
                    museumId = museumId,
                    name = name,
                    address = address,
                    city = city,
                    country = country,
                    website = binding.editTextWebsite.text.toString().trim().takeIf { it.isNotBlank() }
                )
            )
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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
