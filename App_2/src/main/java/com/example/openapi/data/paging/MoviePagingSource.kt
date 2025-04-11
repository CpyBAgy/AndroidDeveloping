package com.example.openapi.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.openapi.data.api.MovieApi
import com.example.openapi.domain.models.Movie
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class MoviePagingSource @Inject constructor(
    private val movieApi: MovieApi,
    private val apiKey: String
) : PagingSource<Int, Movie>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: 1

        return try {
            val response = movieApi.getPopularMovies(apiKey, page)
            val movies = response.results.map { movieDto ->
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

            LoadResult.Page(
                data = movies,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.results.isEmpty()) null else page + 1
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}