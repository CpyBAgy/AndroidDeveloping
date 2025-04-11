package com.example.openapi.domain.repositories

import androidx.paging.PagingData
import com.example.openapi.domain.models.Movie
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    suspend fun getPopularMovies(page: Int): List<Movie>
    fun getPopularMoviesPaging(): Flow<PagingData<Movie>>
}