package com.example.customproject.data.remote.tmdb.dto

import com.google.gson.annotations.SerializedName

data class TmdbVideosResponse(
    @SerializedName("results") val results: List<TmdbVideoDto>
)

data class TmdbVideoDto(
    @SerializedName("key") val key: String,
    @SerializedName("site") val site: String,
    @SerializedName("type") val type: String,
    @SerializedName("official") val official: Boolean = false
)
