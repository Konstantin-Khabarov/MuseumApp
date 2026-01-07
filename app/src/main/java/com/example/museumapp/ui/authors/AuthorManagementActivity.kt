package com.example.museumapp.ui.authors

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.museumapp.MuseumApp
import com.example.museumapp.data.model.Author
import com.example.museumapp.databinding.ActivityAuthorManagementBinding
import kotlinx.coroutines.launch

class AuthorManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthorManagementBinding

    private val viewModel: AuthorViewModel by viewModels {
        AuthorViewModelFactory((application as MuseumApp).authorRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthorManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
        setupListeners()
    }

    private fun setupUI() {
        // Восстановление предыдущих значений поиска (если есть)
        val (authorId, name) = viewModel.getCurrentSearchValues()
        binding.editTextAuthorId.setText(authorId)
        binding.editTextAuthorName.setText(name)
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
                // Ничего не делаем
            }
            is AuthorState.Success -> {
                showSearchResults(state.authors)
            }
            is AuthorState.Error -> {
                showToast(state.message)
            }
            is AuthorState.ShowMessage -> {
                showToast(state.message)
            }
            AuthorState.NavigateBack -> {
                finish()
            }
            AuthorState.NavigateToAddAuthor -> {
                navigateToAddAuthor()
            }
            AuthorState.NavigateToEditAuthor -> {
                navigateToEditAuthor()
            }
        }
    }

    private fun setupListeners() {
        binding.btnBackArrow.setOnClickListener {
            viewModel.onEvent(AuthorEvent.NavigateBack)
        }

        binding.btnSearch.setOnClickListener {
            val name = binding.editTextAuthorName.text.toString()
            val authorId = binding.editTextAuthorId.text.toString()

            viewModel.onEvent(
                AuthorEvent.SearchAuthors(name, authorId)
            )
        }

        binding.btnReset.setOnClickListener {
            binding.editTextAuthorName.setText("")
            binding.editTextAuthorId.setText("")
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
        // Временная реализация - показываем в Toast
        // Позже заменить на RecyclerView
        val message = buildString {
            append("Найдено авторов: ${authors.size}\n")
            authors.take(3).forEachIndexed { index, author ->
                append("${index + 1}. ${author.name}\n")
            }
            if (authors.size > 3) {
                append("... и ещё ${authors.size - 3}")
            }
        }

        showToast(message)

        // binding.recyclerViewAuthors.adapter = AuthorAdapter(authors)
    }

    private fun navigateToAddAuthor() {
        showToast("Добавление нового автора")
        // startActivity(Intent(this, AddAuthorActivity::class.java))
    }

    private fun navigateToEditAuthor() {
        showToast("Редактирование информации об авторе")
        // startActivity(Intent(this, EditAuthorActivity::class.java))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}