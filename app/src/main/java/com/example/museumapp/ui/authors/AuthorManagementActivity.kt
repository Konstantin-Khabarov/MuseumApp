package com.example.museumapp.ui.authors

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.museumapp.MuseumApp
import com.example.museumapp.data.auth.AuthManager
import com.example.museumapp.data.model.Author
import com.example.museumapp.databinding.ActivityAuthorManagementBinding
import com.example.museumapp.ui.exhibits.ExhibitEvent
import kotlinx.coroutines.launch

class AuthorManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthorManagementBinding
    private lateinit var authorAdapter: AuthorAdapter

    private val viewModel: AuthorViewModel by viewModels {
        AuthorViewModelFactory((application as MuseumApp).authorRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthorManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupUI()
        setupObservers()
        setupListeners()
    }

    private fun setupUI() {
        // Восстановление предыдущих значений поиска (если есть)
        val name = viewModel.getCurrentSearchValues()
        binding.editTextAuthorName.setText(name)
    }

    private fun setupRecyclerView() {
        authorAdapter = AuthorAdapter { author ->
            // Передаём автора в новый экран
            val intent = Intent(this, AuthorDetailActivity::class.java).apply {
                putExtra("author_id", author.id)
                putExtra("author_name", author.name)
                putExtra("author_bio", author.biography)
                putExtra("author_birth_date", author.birthDate)
                putExtra("author_death_date", author.deathDate)
            }
            startActivity(intent)
        }
        binding.recyclerViewAuthors.adapter = authorAdapter
        binding.recyclerViewAuthors.layoutManager = LinearLayoutManager(this)
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                handleState(state)
            }
        }
    }

    private fun handleState(state: AuthorState) {
        when (state) {
            is AuthorState.Idle -> {
                setLoading(false)
                // Ничего не делаем
            }
            is AuthorState.Loading -> {
                // 🔥 Показываем индикатор загрузки
                setLoading(true)
            }
            is AuthorState.Success -> {
                showSearchResults(state.authors)
                setLoading(false)
            }
            is AuthorState.Error -> {
                showToast(state.message)
                setLoading(false)
            }
            is AuthorState.ShowMessage -> {
                showToast(state.message)
                setLoading(false)
            }
            AuthorState.NavigateBack -> {
                finish()
            }
            AuthorState.NavigateToAddAuthor -> {
                navigateToAddAuthor()
                viewModel.resetState()
            }
            AuthorState.AuthorAdded -> {
                showToast("Автор добавлен")
                viewModel.onEvent(AuthorEvent.LoadAllAuthors) // Обновить список
                viewModel.resetState()
            }
            AuthorState.NavigateToEditAuthor -> {
                navigateToEditAuthor()
            }
            else -> {}
        }
    }


    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    private fun setupListeners() {
        binding.btnBackArrow.setOnClickListener {
            viewModel.onEvent(AuthorEvent.NavigateBack)
        }

        binding.btnSearch.setOnClickListener {
            val name = binding.editTextAuthorName.text.toString()
            viewModel.onEvent(
                AuthorEvent.SearchAuthors(name)
            )
        }

        binding.btnReset.setOnClickListener {
            binding.editTextAuthorName.setText("")
            viewModel.onEvent(AuthorEvent.ResetSearch)
        }

        binding.btnAddAuthor.setOnClickListener {
            viewModel.onEvent(AuthorEvent.AddAuthor)
        }

        // Кнопка редактирования
        /*binding.btnEditAuthor.setOnClickListener {
            viewModel.onEvent(AuthorEvent.EditAuthor)
        }*/
    }

    private fun showSearchResults(authors: List<Author>) {
        authorAdapter.submitList(authors)

    }

    private fun navigateToAddAuthor() {
        val intent = Intent(this, AddAuthorActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToEditAuthor() {
        showToast("Редактирование информации об авторе")
        // startActivity(Intent(this, EditAuthorActivity::class.java))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}