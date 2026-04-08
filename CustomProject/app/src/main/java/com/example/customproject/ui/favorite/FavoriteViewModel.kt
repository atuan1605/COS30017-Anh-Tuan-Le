package com.example.customproject.ui.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.customproject.data.model.Movie
import com.example.customproject.data.model.PagingState
import com.example.customproject.data.repository.FavoriteRepository
import com.example.customproject.data.repository.MovieRepository
import com.example.customproject.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val movieRepository: MovieRepository
) : ViewModel() {

    private val _favoritesState = MutableStateFlow(PagingState<Movie>())
    val favoritesState: StateFlow<PagingState<Movie>> = _favoritesState

    fun loadFavorites(userId: String) {
        viewModelScope.launch {
            _favoritesState.value = PagingState(isRefreshing = true)
            try {
                val favDtos = favoriteRepository.getFavorites(userId, 1, Constants.PAGE_SIZE_MOVIES)
                // Fetch movie details in parallel — LRU cache avoids re-fetching cached movies
                val movies = favDtos.map { dto ->
                    async { runCatching { movieRepository.getMovieDetail(dto.tmdbId) }.getOrNull() }
                }.awaitAll().filterNotNull()

                _favoritesState.value = PagingState(
                    items = movies,
                    currentPage = 1,
                    hasMore = favDtos.size >= Constants.PAGE_SIZE_MOVIES
                )
            } catch (e: Exception) {
                _favoritesState.value = PagingState(error = e.message)
            }
        }
    }

    fun loadMoreFavorites(userId: String) {
        val state = _favoritesState.value
        if (state.isLoadingMore || !state.hasMore) return
        viewModelScope.launch {
            _favoritesState.value = state.copy(isLoadingMore = true)
            try {
                val nextPage = state.currentPage + 1
                val favDtos = favoriteRepository.getFavorites(userId, nextPage, Constants.PAGE_SIZE_MOVIES)
                val movies = favDtos.map { dto ->
                    async { runCatching { movieRepository.getMovieDetail(dto.tmdbId) }.getOrNull() }
                }.awaitAll().filterNotNull()

                _favoritesState.value = state.copy(
                    items = state.items + movies,
                    currentPage = nextPage,
                    hasMore = favDtos.size >= Constants.PAGE_SIZE_MOVIES,
                    isLoadingMore = false
                )
            } catch (e: Exception) {
                _favoritesState.value = state.copy(isLoadingMore = false, error = e.message)
            }
        }
    }
}
