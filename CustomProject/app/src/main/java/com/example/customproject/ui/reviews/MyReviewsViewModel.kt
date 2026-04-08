package com.example.customproject.ui.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.customproject.data.model.PagingState
import com.example.customproject.data.model.Review
import com.example.customproject.data.repository.MovieRepository
import com.example.customproject.data.repository.ReviewRepository
import com.example.customproject.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyReviewsViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val movieRepository: MovieRepository
) : ViewModel() {

    private val _reviewsState = MutableStateFlow(PagingState<Review>())
    val reviewsState: StateFlow<PagingState<Review>> = _reviewsState

    private val _totalCount = MutableStateFlow(0L)
    val totalCount: StateFlow<Long> = _totalCount

    fun loadReviews(userId: String) {
        viewModelScope.launch {
            _reviewsState.value = PagingState(isRefreshing = true)
            try {
                val count = reviewRepository.getMyReviewCount(userId)
                _totalCount.value = count

                val reviews = reviewRepository.getMyReviews(userId, 1, Constants.PAGE_SIZE_REVIEWS)
                val enriched = enrichWithMovieTitles(reviews)
                _reviewsState.value = PagingState(
                    items = enriched,
                    currentPage = 1,
                    hasMore = reviews.size >= Constants.PAGE_SIZE_REVIEWS
                )
            } catch (e: Exception) {
                _reviewsState.value = PagingState(error = e.message)
            }
        }
    }

    fun loadMoreReviews(userId: String) {
        val state = _reviewsState.value
        if (state.isLoadingMore || !state.hasMore) return
        viewModelScope.launch {
            _reviewsState.value = state.copy(isLoadingMore = true)
            try {
                val nextPage = state.currentPage + 1
                val reviews = reviewRepository.getMyReviews(userId, nextPage, Constants.PAGE_SIZE_REVIEWS)
                val enriched = enrichWithMovieTitles(reviews)
                _reviewsState.value = state.copy(
                    items = state.items + enriched,
                    currentPage = nextPage,
                    hasMore = reviews.size >= Constants.PAGE_SIZE_REVIEWS,
                    isLoadingMore = false
                )
            } catch (e: Exception) {
                _reviewsState.value = state.copy(isLoadingMore = false)
            }
        }
    }

    fun updateReview(reviewId: Long, rating: Double, content: String) {
        val current = _reviewsState.value.items.toMutableList()
        val index = current.indexOfFirst { it.id == reviewId }
        if (index != -1) {
            current[index] = current[index].copy(rating = rating, content = content)
            _reviewsState.value = _reviewsState.value.copy(items = current)
        }
        viewModelScope.launch {
            try {
                reviewRepository.updateReview(reviewId, rating, content)
            } catch (_: Exception) {}
        }
    }

    // Optimistic delete — removes locally first, then syncs with Supabase
    fun deleteReview(userId: String, reviewId: Long) {
        val current = _reviewsState.value.items.toMutableList()
        current.removeAll { it.id == reviewId }
        _reviewsState.value = _reviewsState.value.copy(items = current)
        _totalCount.value = (_totalCount.value - 1).coerceAtLeast(0)
        viewModelScope.launch {
            try { reviewRepository.deleteReview(reviewId) } catch (_: Exception) {
                loadReviews(userId)  // Reload on failure to restore state
            }
        }
    }

    private suspend fun enrichWithMovieTitles(reviews: List<Review>): List<Review> {
        return reviews.map { review ->
            try {
                val movie = movieRepository.getMovieDetail(review.tmdbId)
                review.copy(movieTitle = movie.title, moviePosterPath = movie.posterPath)
            } catch (_: Exception) {
                review
            }
        }
    }
}
