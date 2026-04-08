package com.example.customproject.data.remote.tmdb.dto

import com.google.gson.annotations.SerializedName

data class TmdbCreditsResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("cast") val cast: List<TmdbCastDto>
)

data class TmdbCastDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("character") val character: String,
    @SerializedName("profile_path") val profilePath: String? = null
)
