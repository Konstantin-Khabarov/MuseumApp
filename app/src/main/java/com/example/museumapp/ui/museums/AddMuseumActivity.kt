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
import com.example.museumapp.databinding.ActivityAddMuseumBinding
import kotlinx.coroutines.launch
import com.example.museumapp.ui.main.MainActivity

class AddMuseumActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMuseumBinding
    private val viewModel: MuseumViewModel by viewModels {
        MuseumViewModelFactory((application as MuseumApp).museumRepository, (application as MuseumApp).hallRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMuseumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            if (!AuthManager.isAuthenticated()) {
                showToast("Для добавления необходимо войти в систему")
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
                MuseumEvent.SaveMuseum(
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
                    is MuseumState.MuseumAdded -> {
                        showToast("Музей добавлен")
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
