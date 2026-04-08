package com.example.customproject.data.repository

import android.util.LruCache
import com.example.customproject.data.model.Cast
import com.example.customproject.data.model.Movie
import com.example.customproject.data.model.PagedResult
import com.example.customproject.data.remote.tmdb.TmdbApiService
import com.example.customproject.data.remote.tmdb.dto.TmdbMovieDto
import com.example.customproject.util.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepository @Inject constructor(
    private val api: TmdbApiService
) {
    // In-memory LRU cache for movie details — avoids re-fetching on back navigation
    private val cache = LruCache<Int, Movie>(100)

    suspend fun getTrending(page: Int = 1): PagedResult<Movie> {
        val response = api.getTrending(page)
        return PagedResult(
            items = response.results.map { it.toDomain() },
            currentPage = response.page,
            totalPages = response.totalPages
        )
    }

    suspend fun getNowPlaying(page: Int = 1): PagedResult<Movie> {
        val response = api.getNowPlaying(page)
        return PagedResult(
            items = response.results.map { it.toDomain() },
            currentPage = response.page,
            totalPages = response.totalPages
        )
    }

    suspend fun getMovieDetail(movieId: Int): Movie {
        cache.get(movieId)?.let { return it }
        val dto = api.getMovieDetail(movieId)
        val movie = dto.toDomain()
        cache.put(movieId, movie)
        return movie
    }

    suspend fun getCredits(movieId: Int): List<Cast> {
        val response = api.getMovieCredits(movieId)
        return response.cast.take(15).map {
            Cast(
                id = it.id,
                name = it.name,
                character = it.character,
                profilePath = it.profilePath?.let { p -> "${Constants.TMDB_IMAGE_BASE_URL}w185$p" }
            )
        }
    }

    suspend fun searchMovies(query: String, page: Int = 1): PagedResult<Movie> {
        val response = api.searchMovies(query, page)
        return PagedResult(
            items = response.results.map { it.toDomain() },
            currentPage = response.page,
            totalPages = response.totalPages
        )
    }

    suspend fun discoverByGenre(genreId: Int, page: Int = 1): PagedResult<Movie> {
        val response = api.discoverMovies(genreId = genreId, page = page)
        return PagedResult(
            items = response.results.map { it.toDomain() },
            currentPage = response.page,
            totalPages = response.totalPages
        )
    }

    /**
     * Returns the YouTube key for the best available trailer, or null.
     * Preference: official Trailer → official Teaser → any YouTube video.
     */
    suspend fun getTrailerYoutubeKey(movieId: Int): String? {
        val videos = api.getMovieVideos(movieId).results
            .filter { it.site.equals("YouTube", ignoreCase = true) }
        return videos.firstOrNull { it.type == "Trailer" && it.official }?.key
            ?: videos.firstOrNull { it.type == "Teaser" && it.official }?.key
            ?: videos.firstOrNull { it.type == "Trailer" }?.key
            ?: videos.firstOrNull()?.key
    }

    fun clearCache() = cache.evictAll()

    private fun TmdbMovieDto.toDomain() = Movie(
        id = id,
        title = title ?: "",
        overview = overview ?: "",
        posterPath = posterPath?.let { "${Constants.TMDB_IMAGE_BASE_URL}w500$it" },
        backdropPath = backdropPath?.let { "${Constants.TMDB_IMAGE_BASE_URL}w780$it" },
        releaseDate = releaseDate ?: "",
        voteAverage = voteAverage,
        runtime = runtime,
        // trending/search endpoints return genre_ids (not genres), so genres can be null via Gson
        genres = genres?.map { it.name } ?: emptyList()
    )
}
