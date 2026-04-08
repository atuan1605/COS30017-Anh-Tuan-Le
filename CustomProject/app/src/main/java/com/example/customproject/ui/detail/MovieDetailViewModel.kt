package com.example.customproject.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.customproject.data.model.Cast
import com.example.customproject.data.model.Movie
import com.example.customproject.data.model.PagingState
import com.example.customproject.data.model.Review
import com.example.customproject.data.model.UiState
import com.example.customproject.data.repository.FavoriteRepository
import com.example.customproject.data.repository.MovieRepository
import com.example.customproject.data.repository.ReviewRepository
import com.example.customproject.data.repository.WatchlistRepository
import com.example.customproject.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val reviewRepository: ReviewRepository,
    private val favoriteRepository: FavoriteRepository,
    private val watchlistRepository: WatchlistRepository
) : ViewModel() {

    private val _movieState = MutableStateFlow<UiState<Movie>>(UiState.Loading)
    val movieState: StateFlow<UiState<Movie>> = _movieState

    private val _castState = MutableStateFlow<List<Cast>>(emptyList())
    val castState: StateFlow<List<Cast>> = _castState

    private val _reviewsState = MutableStateFlow(PagingState<Review>())
    val reviewsState: StateFlow<PagingState<Review>> = _reviewsState

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    private val _isInWatchlist = MutableStateFlow(false)
    val isInWatchlist: StateFlow<Boolean> = _isInWatchlist

    private val _reviewSubmitState = MutableStateFlow<UiState<Unit>?>(null)
    val reviewSubmitState: StateFlow<UiState<Unit>?> = _reviewSubmitState

    private val _trailerEvent = MutableSharedFlow<String?>()
    val trailerEvent: SharedFlow<String?> = _trailerEvent

    fun loadMovie(movieId: Int, userId: String?) {
        viewModelScope.launch {
            _movieState.value = UiState.Loading
            try {
                val movie = movieRepository.getMovieDetail(movieId)
                _movieState.value = UiState.Success(movie)
                loadCredits(movieId)
                loadReviews(movieId)
                userId?.let {
                    checkFavoriteStatus(it, movieId)
                    checkWatchlistStatus(it, movieId)
                }
            } catch (e: Exception) {
                _movieState.value = UiState.Error(e.message ?: "Failed to load movie")
            }
        }
    }

    private fun loadCredits(movieId: Int) {
        viewModelScope.launch {
            try {
                _castState.value = movieRepository.getCredits(movieId)
            } catch (_: Exception) {}
        }
    }

    fun loadReviews(movieId: Int) {
        viewModelScope.launch {
            _reviewsState.value = PagingState(isRefreshing = true)
            try {
                val reviews = reviewRepository.getReviewsForMovie(movieId, 1, Constants.PAGE_SIZE_REVIEWS)
                _reviewsState.value = PagingState(
                    items = reviews,
                    currentPage = 1,
                    hasMore = reviews.size >= Constants.PAGE_SIZE_REVIEWS
                )
            } catch (e: Exception) {
                _reviewsState.value = PagingState(error = e.message)
            }
        }
    }

    fun loadMoreReviews(movieId: Int) {
        val state = _reviewsState.value
        if (state.isLoadingMore || !state.hasMore) return
        viewModelScope.launch {
            _reviewsState.value = state.copy(isLoadingMore = true)
            try {
                val nextPage = state.currentPage + 1
                val reviews = reviewRepository.getReviewsForMovie(movieId, nextPage, Constants.PAGE_SIZE_REVIEWS)
                _reviewsState.value = state.copy(
                    items = state.items + reviews,
                    currentPage = nextPage,
                    hasMore = reviews.size >= Constants.PAGE_SIZE_REVIEWS,
                    isLoadingMore = false
                )
            } catch (e: Exception) {
                _reviewsState.value = state.copy(isLoadingMore = false)
            }
        }
    }

    fun submitReview(userId: String, tmdbId: Int, rating: Double, content: String) {
        viewModelScope.launch {
            _reviewSubmitState.value = UiState.Loading
            try {
                reviewRepository.addReview(userId, tmdbId, rating, content)
                _reviewSubmitState.value = UiState.Success(Unit)
                loadReviews(tmdbId)
            } catch (e: Exception) {
                _reviewSubmitState.value = UiState.Error(e.message ?: "Failed to submit review")
            }
        }
    }

    fun toggleFavorite(userId: String, movieId: Int) {
        viewModelScope.launch {
            try {
                if (_isFavorite.value) {
                    favoriteRepository.removeFavorite(userId, movieId)
                    _isFavorite.value = false
                } else {
                    favoriteRepository.addFavorite(userId, movieId)
                    _isFavorite.value = true
                }
            } catch (_: Exception) {}
        }
    }

    fun toggleWatchlist(userId: String, movieId: Int) {
        viewModelScope.launch {
            try {
                if (_isInWatchlist.value) {
                    watchlistRepository.removeFromWatchlist(userId, movieId)
                    _isInWatchlist.value = false
                } else {
                    watchlistRepository.addToWatchlist(userId, movieId)
                    _isInWatchlist.value = true
                }
            } catch (_: Exception) {}
        }
    }

    private suspend fun checkFavoriteStatus(userId: String, movieId: Int) {
        try { _isFavorite.value = favoriteRepository.isFavorite(userId, movieId) } catch (_: Exception) {}
    }

    private suspend fun checkWatchlistStatus(userId: String, movieId: Int) {
        try { _isInWatchlist.value = watchlistRepository.isInWatchlist(userId, movieId) } catch (_: Exception) {}
    }

    fun playTrailer(movieId: Int) {
        viewModelScope.launch {
            try {
                val key = movieRepository.getTrailerYoutubeKey(movieId)
                _trailerEvent.emit(key)
            } catch (_: Exception) {
                _trailerEvent.emit(null)
            }
        }
    }

    fun clearReviewSubmitState() { _reviewSubmitState.value = null }
}
