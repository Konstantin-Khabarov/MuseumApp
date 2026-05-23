package com.example.museumapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.museumapp.data.auth.AuthManager
import com.example.museumapp.databinding.ActivityLoginBinding
import com.example.museumapp.ui.main.MainActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (AuthManager.isAuthenticated()) {
            navigateToMain()
            return
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString()

            if (validateInput(email, password)) {
                performLogin(email, password)
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                binding.editTextEmail.error = "Введите email"
                binding.editTextEmail.requestFocus()
                false
            }
            password.isEmpty() -> {
                binding.editTextPassword.error = "Введите пароль"
                binding.editTextPassword.requestFocus()
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.editTextEmail.error = "Некорректный email"
                binding.editTextEmail.requestFocus()
                false
            }
            else -> true
        }
    }

    private fun performLogin(email: String, password: String) {
        setLoading(true)

        lifecycleScope.launch {
            val result = AuthManager.login(email, password)

            setLoading(false)

            result
                .onSuccess {
                    showToast("Вход выполнен успешно")
                    navigateToMain()
                }
                .onFailure { error ->

                    val message = when {

                        error.message?.contains("Invalid login credentials", ignoreCase = true) == true -> {
                            "Неверный email или пароль"
                        }
                        error.message?.contains("Email not confirmed", ignoreCase = true) == true -> {
                            "Подтвердите адрес электронной почты"
                        }
                        error is java.net.SocketTimeoutException ||
                                error.message?.contains("timeout", ignoreCase = true) == true -> {
                            "Превышено время ожидания. Проверьте соединение"
                        }
                        error is java.net.UnknownHostException -> {
                            "Нет подключения к интернету"
                        }
                        else -> "Ошибка входа: ${error.message ?: "Неизвестная ошибка"}"
                    }
                    showToast(message)

                }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        binding.editTextEmail.isEnabled = !isLoading
        binding.editTextPassword.isEnabled = !isLoading
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
