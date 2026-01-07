package com.example.museumapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.museumapp.ui.main.MainActivity
import com.google.android.material.textfield.TextInputEditText

class RegistrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        // Кнопка подтверждения регистрации
        findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            val username = findViewById<TextInputEditText>(R.id.editTextUsername).text.toString()
            val password = findViewById<TextInputEditText>(R.id.editTextPassword).text.toString()

            if (validateInput(username, password)) {
                registerUser(username, password)
            }
        }

        findViewById<ImageButton>(R.id.btnBackArrow).setOnClickListener {
            finish()
        }
    }

    private fun validateInput(username: String, password: String): Boolean {
        return when {
            username.isEmpty() -> {
                showToast("Введите логин")
                false
            }
            password.isEmpty() -> {
                showToast("Введите пароль")
                false
            }
            else -> true
        }
    }

    private fun registerUser(username: String, password: String) {
        // Здесь будет логика регистрации пользователя
        // Например, сохранение в SharedPreferences или отправка на сервер

        showToast("Регистрация успешна для пользователя: $username")

        // После успешной регистрации возвращаемся на главный экран
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}