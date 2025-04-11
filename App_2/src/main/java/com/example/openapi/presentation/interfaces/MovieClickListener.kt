package com.example.openapi.presentation.interfaces

import com.example.openapi.domain.models.Movie

interface MovieClickListener {
    fun onMovieClicked(movie: Movie)
}