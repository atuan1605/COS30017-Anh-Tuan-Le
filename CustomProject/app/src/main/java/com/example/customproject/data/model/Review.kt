package com.example.customproject.data.model

data class Review(
    val id: Long,
    val userId: String,
    val tmdbId: Int,
    val rating: Double?,
    val content: String,
    val createdAt: String,
    val updatedAt: String,
    val username: String?,
    val avatarUrl: String?,
    val movieTitle: String? = null,
    val moviePosterPath: String? = null
) {
    val ratingOutOf5: Float get() = ((rating ?: 0.0) / 2).toFloat().coerceIn(0f, 5f)
    val ratingLabel: String get() = when {
        (rating ?: 0.0) >= 8.0 -> "Very good"
        (rating ?: 0.0) >= 6.0 -> "Good"
        (rating ?: 0.0) >= 4.0 -> "Average"
        else -> "Poor"
    }
}
