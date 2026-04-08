package com.example.customproject.data.remote.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReviewDto(
    val id: Long = 0L,
    @SerialName("user_id") val userId: String = "",
    @SerialName("tmdb_id") val tmdbId: Int = 0,
    val rating: Double? = null,
    val content: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    val profiles: ProfileSummaryDto? = null
)

@Serializable
data class ProfileSummaryDto(
    val username: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null
)
