package com.example.customproject.data.model

data class PagedResult<T>(
    val items: List<T>,
    val currentPage: Int,
    val totalPages: Int,
    val hasMore: Boolean = currentPage < totalPages
)
