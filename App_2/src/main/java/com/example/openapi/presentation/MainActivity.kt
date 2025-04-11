package com.example.openapi.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.openapi.R
import com.example.openapi.databinding.ActivityMainBinding
import com.example.openapi.databinding.DialogMovieDetailsBinding
import com.example.openapi.domain.models.Movie
import com.example.openapi.presentation.adapters.MovieAdapter
import com.example.openapi.presentation.interfaces.MovieClickListener
import com.example.openapi.presentation.viewmodels.MovieViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), MovieClickListener {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MovieViewModel by viewModels()
    private lateinit var movieAdapter: MovieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter(this)
        binding.rvMovies.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = movieAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            movieAdapter.refresh()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.moviesPagingFlow.collectLatest { pagingData ->
                movieAdapter.submitData(pagingData)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    override fun onMovieClicked(movie: Movie) {
        showMovieDetailsDialog(movie)
    }

    private fun showMovieDetailsDialog(movie: Movie) {
        val dialogBinding = DialogMovieDetailsBinding.inflate(layoutInflater)

        dialogBinding.tvMovieTitle.text = movie.title
        dialogBinding.tvReleaseDate.text = "Released: ${movie.releaseDate}"
        dialogBinding.tvRatingValue.text = "${movie.rating}/10"
        dialogBinding.ratingBar.rating = movie.rating / 2 // Convert 10-scale to 5-scale
        dialogBinding.tvOverview.text = movie.overview

        Glide.with(this)
            .load(movie.posterUrl)
            .placeholder(R.drawable.ic_movie_placeholder)
            .error(R.drawable.ic_movie_placeholder)
            .into(dialogBinding.ivMoviePoster)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}