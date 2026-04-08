package com.example.customproject.data.remote.tmdb.dto

import com.google.gson.annotations.SerializedName

data class TmdbPagedResponse<T>(
    @SerializedName("page") val page: Int,
    @SerializedName("results") val results: List<T>,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_results") val totalResults: Int
)
