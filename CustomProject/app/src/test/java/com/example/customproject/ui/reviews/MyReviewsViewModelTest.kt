package com.example.customproject.ui.reviews

import com.example.customproject.data.model.Movie
import com.example.customproject.data.model.Review
import com.example.customproject.data.repository.MovieRepository
import com.example.customproject.data.repository.ReviewRepository
import com.example.customproject.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for MyReviewsViewModel.
 *
 * Uses MockK to fake repositories and MainDispatcherRule to run
 * viewModelScope coroutines synchronously on the test thread.
 *
 * NOTE: StateFlow is conflated — intermediate loading states may be
 * skipped by the time an assertion runs. Tests therefore assert on
 * the final settled value rather than on transient emissions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MyReviewsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val reviewRepository: ReviewRepository = mockk()
    private val movieRepository: MovieRepository = mockk()

    private lateinit var viewModel: MyReviewsViewModel

    private val sampleReviews = listOf(
        makeReview(id = 1L, tmdbId = 550, rating = 8.0, content = "Great film"),
        makeReview(id = 2L, tmdbId = 680, rating = 6.0, content = "Pretty good"),
        makeReview(id = 3L, tmdbId = 13,  rating = 4.0, content = "Average")
    )

    private val sampleMovie = Movie(
        id = 550, title = "Fight Club", posterPath = "/poster.jpg",
        backdropPath = null, overview = "", releaseDate = "1999-10-15",
        voteAverage = 8.4, runtime = 139, genres = listOf("Drama", "Thriller")
    )

    @Before
    fun setup() {
        viewModel = MyReviewsViewModel(reviewRepository, movieRepository)
    }

    // ── loadReviews ───────────────────────────────────────────────

    @Test
    fun `loadReviews success - items are populated`() = runTest {
        coEvery { reviewRepository.getMyReviewCount("user-1") } returns 3L
        coEvery { reviewRepository.getMyReviews("user-1", 1, any()) } returns sampleReviews
        coEvery { movieRepository.getMovieDetail(any()) } returns sampleMovie

        viewModel.loadReviews("user-1")

        with(viewModel.reviewsState.value) {
            assertEquals(3, items.size)
            assertEquals(false, isRefreshing)
            assertNull(error)
        }
    }

    @Test
    fun `loadReviews success - totalCount matches repository`() = runTest {
        coEvery { reviewRepository.getMyReviewCount("user-1") } returns 5L
        coEvery { reviewRepository.getMyReviews("user-1", 1, any()) } returns sampleReviews
        coEvery { movieRepository.getMovieDetail(any()) } returns sampleMovie

        viewModel.loadReviews("user-1")

        assertEquals(5L, viewModel.totalCount.value)
    }

    @Test
    fun `loadReviews error - state contains error message and empty list`() = runTest {
        coEvery { reviewRepository.getMyReviewCount("user-1") } throws RuntimeException("Network error")

        viewModel.loadReviews("user-1")

        with(viewModel.reviewsState.value) {
            assertEquals("Network error", error)
            assertTrue(items.isEmpty())
            assertEquals(false, isRefreshing)
        }
    }

    @Test
    fun `loadReviews success - hasMore is false when fewer items than page size`() = runTest {
        val fewReviews = sampleReviews.take(2)   // 2 < PAGE_SIZE_REVIEWS (10)
        coEvery { reviewRepository.getMyReviewCount("user-1") } returns 2L
        coEvery { reviewRepository.getMyReviews("user-1", 1, any()) } returns fewReviews
        coEvery { movieRepository.getMovieDetail(any()) } returns sampleMovie

        viewModel.loadReviews("user-1")

        assertEquals(false, viewModel.reviewsState.value.hasMore)
    }

    // ── deleteReview ──────────────────────────────────────────────

    @Test
    fun `deleteReview removes item from list immediately (optimistic)`() = runTest {
        setupLoadedState()
        coEvery { reviewRepository.deleteReview(1L) } returns Unit

        viewModel.deleteReview("user-1", reviewId = 1L)

        val items = viewModel.reviewsState.value.items
        assertEquals(2, items.size)
        assertTrue(items.none { it.id == 1L })
    }

    @Test
    fun `deleteReview decrements totalCount by one`() = runTest {
        setupLoadedState()
        coEvery { reviewRepository.deleteReview(any()) } returns Unit

        val countBefore = viewModel.totalCount.value
        viewModel.deleteReview("user-1", reviewId = 2L)

        assertEquals(countBefore - 1, viewModel.totalCount.value)
    }

    @Test
    fun `deleteReview totalCount never goes below zero`() = runTest {
        coEvery { reviewRepository.getMyReviewCount("user-1") } returns 0L
        coEvery { reviewRepository.getMyReviews("user-1", 1, any()) } returns emptyList()
        viewModel.loadReviews("user-1")

        coEvery { reviewRepository.deleteReview(any()) } returns Unit
        viewModel.deleteReview("user-1", reviewId = 99L)

        assertEquals(0L, viewModel.totalCount.value)
    }

    @Test
    fun `deleteReview calls repository with correct id`() = runTest {
        setupLoadedState()
        coEvery { reviewRepository.deleteReview(1L) } returns Unit

        viewModel.deleteReview("user-1", reviewId = 1L)

        coVerify(exactly = 1) { reviewRepository.deleteReview(1L) }
    }

    // ── updateReview ──────────────────────────────────────────────

    @Test
    fun `updateReview changes rating and content immediately (optimistic)`() = runTest {
        setupLoadedState()
        coEvery { reviewRepository.updateReview(1L, any(), any()) } returns Unit

        viewModel.updateReview(reviewId = 1L, rating = 9.0, content = "Even better on rewatch")

        val updated = viewModel.reviewsState.value.items.first { it.id == 1L }
        assertEquals(9.0, updated.rating!!, 0.001)
        assertEquals("Even better on rewatch", updated.content)
    }

    @Test
    fun `updateReview does not affect other reviews`() = runTest {
        setupLoadedState()
        coEvery { reviewRepository.updateReview(any(), any(), any()) } returns Unit

        viewModel.updateReview(reviewId = 1L, rating = 9.0, content = "Updated")

        val review2 = viewModel.reviewsState.value.items.first { it.id == 2L }
        assertEquals("Pretty good", review2.content)
        assertEquals(6.0, review2.rating!!, 0.001)
    }

    @Test
    fun `updateReview calls repository with correct arguments`() = runTest {
        setupLoadedState()
        coEvery { reviewRepository.updateReview(1L, 9.0, "Updated content") } returns Unit

        viewModel.updateReview(reviewId = 1L, rating = 9.0, content = "Updated content")

        coVerify(exactly = 1) { reviewRepository.updateReview(1L, 9.0, "Updated content") }
    }

    // ── helpers ───────────────────────────────────────────────────

    private suspend fun setupLoadedState() {
        coEvery { reviewRepository.getMyReviewCount("user-1") } returns sampleReviews.size.toLong()
        coEvery { reviewRepository.getMyReviews("user-1", 1, any()) } returns sampleReviews
        coEvery { movieRepository.getMovieDetail(any()) } returns sampleMovie
        viewModel.loadReviews("user-1")
    }

    private fun makeReview(id: Long, tmdbId: Int, rating: Double, content: String) = Review(
        id = id, userId = "user-1", tmdbId = tmdbId, rating = rating, content = content,
        createdAt = "2024-01-01T00:00:00Z", updatedAt = "2024-01-01T00:00:00Z",
        username = "testuser", avatarUrl = null
    )
}
