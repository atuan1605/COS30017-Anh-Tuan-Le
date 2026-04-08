package com.example.customproject.data.remote.tmdb.dto

import com.google.gson.annotations.SerializedName

data class TmdbMovieDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String = "",
    @SerializedName("overview") val overview: String = "",
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    @SerializedName("release_date") val releaseDate: String = "",
    @SerializedName("vote_average") val voteAverage: Double = 0.0,
    @SerializedName("runtime") val runtime: Int? = null,
    @SerializedName("genres") val genres: List<TmdbGenreDto>? = null
)

data class TmdbGenreDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)
