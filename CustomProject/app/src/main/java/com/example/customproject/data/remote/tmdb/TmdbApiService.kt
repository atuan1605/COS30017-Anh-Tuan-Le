package com.example.customproject.data.remote.tmdb

import com.example.customproject.data.remote.tmdb.dto.TmdbCreditsResponse
import com.example.customproject.data.remote.tmdb.dto.TmdbMovieDto
import com.example.customproject.data.remote.tmdb.dto.TmdbPagedResponse
import com.example.customproject.data.remote.tmdb.dto.TmdbVideosResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {

    @GET("trending/movie/week")
    suspend fun getTrending(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMovieDto>

    @GET("movie/now_playing")
    suspend fun getNowPlaying(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMovieDto>

    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String = "en-US"
    ): TmdbMovieDto

    @GET("movie/{movie_id}/credits")
    suspend fun getMovieCredits(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String = "en-US"
    ): TmdbCreditsResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMovieDto>

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("with_genres") genreId: Int,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMovieDto>

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String = "en-US"
    ): TmdbVideosResponse
}
