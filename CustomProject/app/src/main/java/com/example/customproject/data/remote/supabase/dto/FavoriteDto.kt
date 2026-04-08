package com.example.customproject.data.remote.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteDto(
    val id: Long = 0L,
    @SerialName("user_id") val userId: String = "",
    @SerialName("tmdb_id") val tmdbId: Int = 0,
    @SerialName("media_type") val mediaType: String = "movie",
    @SerialName("created_at") val createdAt: String = ""
)
