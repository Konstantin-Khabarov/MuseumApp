package com.example.museumapp.ui.exhibits

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.museumapp.MuseumApp
import com.example.museumapp.R
import com.example.museumapp.data.auth.AuthManager
import com.example.museumapp.data.model.Exhibit
import com.example.museumapp.databinding.ActivityExhibitManagementBinding
import com.example.museumapp.ui.authors.AuthorManagementActivity
import com.example.museumapp.ui.museums.MuseumManagementActivity
import kotlinx.coroutines.launch
import com.example.museumapp.ui.main.MainActivity

class ExhibitManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExhibitManagementBinding
    private lateinit var exhibitAdapter: ExhibitAdapter

    private val viewModel: ExhibitViewModel by viewModels {
        ExhibitViewModelFactory((application as MuseumApp).exhibitRepository, (application as MuseumApp).authorRepository, (application as MuseumApp).museumRepository, (application as MuseumApp).hallRepository)
    }

    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.onEvent(ExhibitEvent.ResetSearch)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityExhibitManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupUI()
        setupObservers()
        setupListeners()
        setupBottomNav()
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNav.selectedItemId = R.id.nav_exhibits
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE

    }

    private fun setupUI() {

        val (title, authorName, museumName) = viewModel.getCurrentSearchValues()
        binding.editTextExhibitName.setText(title)
        binding.editTextAuthorName.setText(authorName)
        binding.editTextMuseumName.setText(museumName)
    }

    private fun setupRecyclerView() {
        exhibitAdapter = ExhibitAdapter { exhibit ->
            val intent = Intent(this, ExhibitDetailActivity::class.java).apply {
                putExtra("exhibit_id", exhibit.id)
                putExtra("exhibit_title", exhibit.title)
                putExtra("exhibit_description", exhibit.description)
                putExtra("exhibit_creation_year", exhibit.creationYear)

                putExtra("exhibit_author_id", exhibit.authorId)
                putExtra("exhibit_museum_id", exhibit.museumId)

                putExtra("exhibit_author_name", exhibit.authorName)
                putExtra("exhibit_museum_name", exhibit.museumName)

            }
            detailLauncher.launch(intent)
        }
        binding.recyclerViewExhibits.adapter = exhibitAdapter
        binding.recyclerViewExhibits.layoutManager = LinearLayoutManager(this)

        binding.recyclerViewExhibits.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!recyclerView.canScrollVertically(1) && viewModel.canLoadMore()) {
                    viewModel.loadNextPage()
                }
            }
        })
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                handleState(state)
            }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect { event ->
                if (event is ExhibitNavigationEvent.ToAddExhibit) navigateToAddExhibit()
            }
            }
        }
    }

    private fun handleState(state: ExhibitState) {
        when (state) {
            is ExhibitState.Idle -> {}
            is ExhibitState.Loading -> {
                setLoading(true)
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
            else -> {}
        }
    }

    private fun setupListeners() {

        binding.btnBackArrow.setOnClickListener {
            viewModel.onEvent(ExhibitEvent.NavigateBack)
        }
        binding.btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        binding.btnSearch.setOnClickListener {
            hideKeyboard()
            val title = binding.editTextExhibitName.text.toString()
            val authorName = binding.editTextAuthorName.text.toString()
            val museumName = binding.editTextMuseumName.text.toString()
            if (title == "" && authorName == "" && museumName == ""){
                showToast("Введите критерии поиска")
            } else{
                setLoading(true)
                viewModel.onEvent(
                    ExhibitEvent.SearchExhibits(title, authorName, museumName)
                )
            }
        }

        binding.btnReset.setOnClickListener {
            binding.editTextExhibitName.setText("")
            binding.editTextAuthorName.setText("")
            binding.editTextMuseumName.setText("")

            setLoading(true)
            viewModel.onEvent(ExhibitEvent.ResetSearch)
        }

        binding.fabAdd.setOnClickListener {
            if (AuthManager.isAuthenticated()) {
                viewModel.onEvent(ExhibitEvent.NavigateToAddExhibit)
            } else {
                showToast("Для добавления экспоната необходимо войти в систему")
            }
        }
    }

    private fun showSearchResults(exhibits: List<Exhibit>) {
        exhibitAdapter.submitList(exhibits)
    }

    private fun navigateToAddExhibit() {
        val intent = Intent(this, AddExhibitActivity::class.java)
        startActivity(intent)
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_exhibits -> true
                R.id.nav_authors -> {
                    startActivity(Intent(this, AuthorManagementActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    })
                    true
                }
                R.id.nav_museums -> {
                    startActivity(Intent(this, MuseumManagementActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    })
                    true
                }
                R.id.nav_halls -> {
                    startActivity(Intent(this, com.example.museumapp.ui.halls.HallManagementActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    })
                    true
                }
                else -> false
            }
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
