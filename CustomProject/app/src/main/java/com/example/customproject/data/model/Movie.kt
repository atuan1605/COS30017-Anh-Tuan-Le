package com.example.customproject.data.model

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val runtime: Int?,
    val genres: List<String>
) {
    val releaseYear: String get() = releaseDate.take(4)
    val formattedRuntime: String
        get() = runtime?.let {
            val h = it / 60
            val m = it % 60
            if (h > 0) "${h}h ${m}m" else "${m}m"
        } ?: "N/A"
    val ratingOutOf5: Float get() = (voteAverage / 2).toFloat().coerceIn(0f, 5f)
}
