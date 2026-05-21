package com.example.museumapp.ui.halls

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.museumapp.MuseumApp
import com.example.museumapp.R
import com.example.museumapp.databinding.ActivityHallManagementBinding
import com.example.museumapp.ui.authors.AuthorManagementActivity
import com.example.museumapp.ui.exhibits.ExhibitManagementActivity
import com.example.museumapp.ui.museums.MuseumManagementActivity
import kotlinx.coroutines.launch

class HallManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHallManagementBinding
    private lateinit var hallAdapter: HallAdapter

    private val viewModel: HallViewModel by viewModels {
        HallViewModelFactory((application as MuseumApp).hallRepository)
    }

    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) viewModel.onEvent(HallEvent.LoadAllHalls)
    }

    private val addLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) viewModel.onEvent(HallEvent.LoadAllHalls)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHallManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupUI()
        setupObservers()
        setupListeners()
        setupBottomNav()
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNav.selectedItemId = R.id.nav_halls
    }

    private fun setupUI() {
        val (number, name, museum) = viewModel.getCurrentSearchValues()
        binding.editTextHallNumber.setText(number)
        binding.editTextHallName.setText(name)
        binding.editTextMuseumName.setText(museum)
    }

    private fun setupRecyclerView() {
        hallAdapter = HallAdapter { hall ->
            val intent = Intent(this, HallDetailActivity::class.java).apply {
                putExtra("hall_id", hall.hallId)
                putExtra("hall_museum_id", hall.museumId)
                putExtra("hall_name", hall.name)
                putExtra("hall_number", hall.hallNumber)
                putExtra("hall_museum_name", hall.museumName)
                putExtra("hall_description", hall.description)
                putExtra("hall_is_storage", hall.isStorage ?: false)
            }
            detailLauncher.launch(intent)
        }
        binding.recyclerViewHalls.adapter = hallAdapter
        binding.recyclerViewHalls.layoutManager = LinearLayoutManager(this)
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is HallState.Loading -> setLoading(true)
                    is HallState.Success -> {
                        setLoading(false)
                        hallAdapter.submitList(state.halls)
                    }
                    is HallState.Error -> {
                        setLoading(false)
                        showToast(state.message)
                    }
                    is HallState.ShowMessage -> {
                        setLoading(false)
                        hallAdapter.submitList(emptyList())
                        showToast(state.message)
                    }
                    is HallState.Idle -> setLoading(false)
                    HallState.HallAdded, HallState.HallUpdated, HallState.HallDeleted -> {}
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnBackArrow.setOnClickListener { finish() }

        binding.btnSearch.setOnClickListener {
            viewModel.onEvent(
                HallEvent.SearchHalls(
                    hallNumber = binding.editTextHallNumber.text.toString(),
                    name = binding.editTextHallName.text.toString(),
                    museumName = binding.editTextMuseumName.text.toString()
                )
            )
        }

        binding.btnReset.setOnClickListener {
            binding.editTextHallNumber.setText("")
            binding.editTextHallName.setText("")
            binding.editTextMuseumName.setText("")
            viewModel.onEvent(HallEvent.ResetSearch)
        }

        binding.btnAddHall.setOnClickListener {
            if (!com.example.museumapp.data.auth.AuthManager.isAuthenticated()) {
                showToast("Для добавления необходимо войти в систему")
                return@setOnClickListener
            }
            addLauncher.launch(Intent(this, AddHallActivity::class.java))
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_halls -> true
                R.id.nav_exhibits -> {
                    startActivity(Intent(this, ExhibitManagementActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    })
                    true
                }
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
                else -> false
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
