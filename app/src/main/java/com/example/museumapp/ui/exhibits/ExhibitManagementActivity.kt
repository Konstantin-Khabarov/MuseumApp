package com.example.museumapp.ui.exhibits

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.museumapp.MuseumApp
import com.example.museumapp.data.model.Exhibit
import com.example.museumapp.databinding.ActivityExhibitManagementBinding
import kotlinx.coroutines.launch

class ExhibitManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExhibitManagementBinding
    private lateinit var exhibitAdapter: ExhibitAdapter

    private val viewModel: ExhibitViewModel by viewModels {
        ExhibitViewModelFactory((application as MuseumApp).exhibitRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityExhibitManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupUI()
        setupObservers()
        setupListeners()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE

        // Блокируем кнопки во время загрузки
        binding.btnSearch.isEnabled = !isLoading
        binding.btnReset.isEnabled = !isLoading
        binding.btnAdd.isEnabled = !isLoading
    }

    private fun setupUI() {
        // Восстановление предыдущих значений поиска
        val (title, authorName, museumName) = viewModel.getCurrentSearchValues()
        binding.editTextExhibitName.setText(title)
        binding.editTextAuthorName.setText(authorName)
        binding.editTextMuseumName.setText(museumName)
    }

    private fun setupRecyclerView() {
        exhibitAdapter = ExhibitAdapter { exhibit ->
            // Передаём экспонат в новый экран детальной информации
            val intent = Intent(this, ExhibitDetailActivity::class.java).apply {
                putExtra("exhibit_id", exhibit.id)
                putExtra("exhibit_title", exhibit.title)
                putExtra("exhibit_description", exhibit.description)
                putExtra("exhibit_creation_year", exhibit.creationYear)
                putExtra("exhibit_author_id", exhibit.authorId)
                putExtra("exhibit_museum_id", exhibit.museumId)
                //putExtra("exhibit_image_url", exhibit.imageUrl)
            }
            startActivity(intent)
        }
        binding.recyclerViewExhibits.adapter = exhibitAdapter
        binding.recyclerViewExhibits.layoutManager = LinearLayoutManager(this)

        // Добавляем слушатель скролла для пагинации
        binding.recyclerViewExhibits.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Если доскроллили до конца и можно грузить ещё
                if (!recyclerView.canScrollVertically(1) && viewModel.canLoadMore()) {
                    viewModel.loadNextPage()
                }
            }
        })
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                handleState(state)
            }
        }
    }

    private fun handleState(state: ExhibitState) {
        when (state) {
            is ExhibitState.Idle -> {
                // Ничего не делаем
            }
            is ExhibitState.Success -> {
                showSearchResults(state.exhibits)
                setLoading(false)
            }
            is ExhibitState.Error -> {
                showToast(state.message)
                setLoading(false)
            }
            is ExhibitState.ShowMessage -> {
                showToast(state.message)
                setLoading(false)
            }
            ExhibitState.NavigateBack -> {
                finish()
            }
            ExhibitState.NavigateToAddExhibit -> {
                navigateToAddExhibit()
            }
        }
    }

    private fun setupListeners() {
        // Кнопка назад
        binding.btnBackArrow.setOnClickListener {
            viewModel.onEvent(ExhibitEvent.NavigateBack)
        }

        // Кнопка поиска
        binding.btnSearch.setOnClickListener {
            val title = binding.editTextExhibitName.text.toString()
            val authorName = binding.editTextAuthorName.text.toString()
            val museumName = binding.editTextMuseumName.text.toString()

            setLoading(true)

            viewModel.onEvent(
                ExhibitEvent.SearchExhibits(title, authorName, museumName)
            )
        }

        // Кнопка сброса
        binding.btnReset.setOnClickListener {
            binding.editTextExhibitName.setText("")
            binding.editTextAuthorName.setText("")
            binding.editTextMuseumName.setText("")

            setLoading(true)
            viewModel.onEvent(ExhibitEvent.ResetSearch)
        }

        // Кнопка добавления нового экспоната
        binding.btnAdd.setOnClickListener {
            viewModel.onEvent(ExhibitEvent.AddExhibit)
        }
    }

    private fun showSearchResults(exhibits: List<Exhibit>) {
        exhibitAdapter.submitList(exhibits)
    }

    private fun navigateToAddExhibit() {
        showToast("Добавление нового экспоната")
        // TODO: Переход к AddExhibitActivity
        // startActivity(Intent(this, AddExhibitActivity::class.java))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}