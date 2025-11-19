package com.example.museumapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //setupButtonListeners()
    }

    /*private fun setupButtonListeners() {
        findViewById<MaterialCardView>(R.id.btnRegistration).setOnClickListener {
            showToast("Регистрация в системе")
            navigateToRegistration()
        }

        setupCardButton(R.id.cardExhibitSearch, "Поиск экспоната", ::navigateToExhibitSearch)
        setupCardButton(R.id.cardAuthorSearch, "Поиск автора", ::navigateToAuthorSearch)
        setupCardButton(R.id.cardMuseumInfo, "Информация о музее", ::navigateToMuseumInfo)
        setupCardButton(R.id.cardExhibitionsInfo, "Информация о выставках", ::navigateToExhibitionsInfo)
    }*/

    /*private fun setupCardButton(cardId: Int, buttonName: String, navigationAction: () -> Unit) {
        findViewById<MaterialCardView>(cardId).setOnClickListener {
            showToast("Выбрано: $buttonName")
            navigationAction()
        }
    }


    private fun navigateToExhibitSearch() {
        showToast("Переход к поиску экспоната")
        // startActivity(Intent(this, ExhibitSearchActivity::class.java))
    }

    private fun navigateToAuthorSearch() {
        showToast("Переход к поиску автора")
        // startActivity(Intent(this, AuthorSearchActivity::class.java))
    }

    private fun navigateToMuseumInfo() {
        showToast("Переход к информации о музее")
        // startActivity(Intent(this, MuseumInfoActivity::class.java))
    }

    private fun navigateToExhibitionsInfo() {
        showToast("Переход к информации о выставках")
        // startActivity(Intent(this, ExhibitionsInfoActivity::class.java))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }*/
}