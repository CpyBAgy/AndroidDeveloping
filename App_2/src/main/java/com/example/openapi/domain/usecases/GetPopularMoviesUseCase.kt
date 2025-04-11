package com.example.openapi.domain.usecases

import androidx.paging.PagingData
import com.example.openapi.domain.models.Movie
import com.example.openapi.domain.repositories.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPopularMoviesUseCase @Inject constructor(
    private val repository: MovieRepository
) {
    operator fun invoke(): Flow<PagingData<Movie>> {
        return repository.getPopularMoviesPaging()
    }
}