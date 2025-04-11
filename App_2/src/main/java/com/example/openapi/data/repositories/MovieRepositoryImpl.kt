package com.example.openapi.data.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.openapi.data.api.MovieApi
import com.example.openapi.data.paging.MoviePagingSource
import com.example.openapi.domain.models.Movie
import com.example.openapi.domain.repositories.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val api: MovieApi
) : MovieRepository {

    companion object {
        private const val API_KEY = "db0474ddb13655577eb0a4458b0ada0d"
        private const val PAGE_SIZE = 20
    }

    override fun getPopularMoviesPaging(): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { MoviePagingSource(api, API_KEY) }
        ).flow
    }

    override suspend fun getPopularMovies(page: Int): List<Movie> {
        return api.getPopularMovies(API_KEY, page).results.map { movieDto ->
            Movie(
                id = movieDto.id,
                title = movieDto.title,
                posterUrl = if (movieDto.posterPath != null)
                    "https://image.tmdb.org/t/p/w500${movieDto.posterPath}"
                else null,
                releaseDate = movieDto.releaseDate ?: "",
                rating = movieDto.rating,
                overview = movieDto.overview ?: "No description available"
            )
        }
    }
}