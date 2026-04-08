package com.example.customproject.data.model

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

data class PagingState<T>(
    val items: List<T> = emptyList(),
    val currentPage: Int = 1,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null
)
