package com.example.customproject.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.customproject.data.model.Movie
import com.example.customproject.data.model.PagingState
import com.example.customproject.data.model.UiState
import com.example.customproject.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val movieRepository: MovieRepository
) : ViewModel() {

    companion object {
        const val DISPLAY_PAGE_SIZE = 9
        const val GRID_SPAN = 3
    }

    private val _featuredMovie = MutableStateFlow<UiState<Movie>>(UiState.Loading)
    val featuredMovie: StateFlow<UiState<Movie>> = _featuredMovie

    private val _trendingState = MutableStateFlow(PagingState<Movie>())
    val trendingState: StateFlow<PagingState<Movie>> = _trendingState

    private val _searchState = MutableStateFlow(PagingState<Movie>())
    val searchState: StateFlow<PagingState<Movie>> = _searchState

    val searchQuery = MutableStateFlow("")

    // Genre filter — null = trending, non-null = discover by genre
    val selectedGenreId = MutableStateFlow<Int?>(null)

    // Emits a YouTube key or empty string (no trailer found) for the play button
    private val _trailerEvent = MutableSharedFlow<String?>()
    val trailerEvent: SharedFlow<String?> = _trailerEvent

    // Client-side buffer to serve 9 items at a time from TMDB's 20-item pages
    private val trendingBuffer = mutableListOf<Movie>()
    private var trendingDisplayCount = 0
    private var tmdbNextPage = 1
    private var tmdbHasMorePages = true

    init {
        loadFeatured()
        observeGenreFilter()
        observeSearch()
    }

    // Reload movies whenever genre filter changes (collectLatest cancels in-flight load)
    private fun observeGenreFilter() {
        viewModelScope.launch {
            selectedGenreId.collectLatest { genreId ->
                loadMoviesInternal(genreId)
            }
        }
    }

    // Retry with the currently active filter
    fun loadTrending() {
        viewModelScope.launch {
            loadMoviesInternal(selectedGenreId.value)
        }
    }

    fun setGenreFilter(genreId: Int?) {
        selectedGenreId.value = genreId
    }

    private suspend fun loadMoviesInternal(genreId: Int?) {
        trendingBuffer.clear()
        trendingDisplayCount = 0
        tmdbNextPage = 1
        tmdbHasMorePages = true
        _trendingState.value = PagingState(isRefreshing = true)
        try {
            fetchToFillBuffer(genreId, DISPLAY_PAGE_SIZE)
            val items = trendingBuffer.take(DISPLAY_PAGE_SIZE)
            trendingDisplayCount = items.size
            _trendingState.value = PagingState(
                items = items,
                hasMore = trendingBuffer.size > trendingDisplayCount || tmdbHasMorePages
            )
        } catch (e: CancellationException) {
            throw e  // Let collectLatest cancel properly; don't show error
        } catch (e: Exception) {
            _trendingState.value = PagingState(error = e.message)
        }
    }

    fun loadMoreTrending() {
        val state = _trendingState.value
        // Block if refreshing (initial load) or already loading more or no more pages
        if (state.isRefreshing || state.isLoadingMore || !state.hasMore) return
        // Set isLoadingMore = true synchronously to prevent duplicate calls
        // before viewModelScope.launch dispatches
        _trendingState.value = state.copy(isLoadingMore = true)
        val genreId = selectedGenreId.value
        viewModelScope.launch {
            try {
                val nextCount = trendingDisplayCount + DISPLAY_PAGE_SIZE
                fetchToFillBuffer(genreId, nextCount)
                val items = trendingBuffer.take(nextCount)
                trendingDisplayCount = items.size
                _trendingState.value = _trendingState.value.copy(
                    items = items,
                    isLoadingMore = false,
                    hasMore = trendingBuffer.size > trendingDisplayCount || tmdbHasMorePages
                )
            } catch (e: Exception) {
                _trendingState.value = _trendingState.value.copy(isLoadingMore = false, error = e.message)
            }
        }
    }

    private suspend fun fetchToFillBuffer(genreId: Int?, targetSize: Int) {
        while (trendingBuffer.size < targetSize && tmdbHasMorePages) {
            val result = if (genreId != null) {
                movieRepository.discoverByGenre(genreId, tmdbNextPage)
            } else {
                movieRepository.getTrending(tmdbNextPage)
            }
            trendingBuffer.addAll(result.items)
            tmdbNextPage = result.currentPage + 1
            tmdbHasMorePages = result.hasMore
        }
    }

    private fun loadFeatured() {
        viewModelScope.launch {
            try {
                val result = movieRepository.getNowPlaying(1)
                val movie = result.items.randomOrNull()
                _featuredMovie.value = if (movie != null) UiState.Success(movie)
                else UiState.Error("No featured movie found")
            } catch (e: Exception) {
                _featuredMovie.value = UiState.Error(e.message ?: "Failed to load")
            }
        }
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

    @OptIn(FlowPreview::class)
    private fun observeSearch() {
        viewModelScope.launch {
            searchQuery
                .debounce(400)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    flow {
                        if (query.isBlank()) {
                            emit(PagingState())
                        } else {
                            emit(PagingState(isRefreshing = true))
                            try {
                                val result = movieRepository.searchMovies(query, 1)
                                emit(PagingState(
                                    items = result.items,
                                    currentPage = result.currentPage,
                                    hasMore = result.hasMore
                                ))
                            } catch (e: Exception) {
                                emit(PagingState(error = e.message))
                            }
                        }
                    }
                }
                .collect { _searchState.value = it }
        }
    }
}
