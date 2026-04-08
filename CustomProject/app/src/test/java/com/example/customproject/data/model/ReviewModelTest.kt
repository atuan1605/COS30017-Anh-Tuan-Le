package com.example.customproject.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure unit tests for Review domain model computed properties.
 * No Android framework or mocking needed — runs entirely on the JVM.
 */
class ReviewModelTest {

    // Helper to build a Review with minimal boilerplate
    private fun makeReview(rating: Double?) = Review(
        id = 1L,
        userId = "user-1",
        tmdbId = 550,
        rating = rating,
        content = "Great movie",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        username = "testuser",
        avatarUrl = null
    )

    // ── ratingOutOf5 ──────────────────────────────────────────────

    @Test
    fun `ratingOutOf5 converts 10-scale to 5-scale correctly`() {
        val review = makeReview(rating = 8.0)
        assertEquals(4.0f, review.ratingOutOf5)
    }

    @Test
    fun `ratingOutOf5 returns 0 when rating is null`() {
        val review = makeReview(rating = null)
        assertEquals(0.0f, review.ratingOutOf5)
    }

    @Test
    fun `ratingOutOf5 clamps to 5 when rating exceeds 10`() {
        val review = makeReview(rating = 12.0)
        assertEquals(5.0f, review.ratingOutOf5)
    }

    @Test
    fun `ratingOutOf5 clamps to 0 for negative rating`() {
        val review = makeReview(rating = -2.0)
        assertEquals(0.0f, review.ratingOutOf5)
    }

    // ── ratingLabel ───────────────────────────────────────────────

    @Test
    fun `ratingLabel returns Very good for rating 8 and above`() {
        assertEquals("Very good", makeReview(rating = 8.0).ratingLabel)
        assertEquals("Very good", makeReview(rating = 10.0).ratingLabel)
    }

    @Test
    fun `ratingLabel returns Good for rating between 6 and 8`() {
        assertEquals("Good", makeReview(rating = 6.0).ratingLabel)
        assertEquals("Good", makeReview(rating = 7.5).ratingLabel)
    }

    @Test
    fun `ratingLabel returns Average for rating between 4 and 6`() {
        assertEquals("Average", makeReview(rating = 4.0).ratingLabel)
        assertEquals("Average", makeReview(rating = 5.9).ratingLabel)
    }

    @Test
    fun `ratingLabel returns Poor for rating below 4`() {
        assertEquals("Poor", makeReview(rating = 3.9).ratingLabel)
        assertEquals("Poor", makeReview(rating = 0.0).ratingLabel)
        assertEquals("Poor", makeReview(rating = null).ratingLabel)
    }

    // ── copy behaviour for optimistic update ──────────────────────

    @Test
    fun `copy with new rating and content preserves other fields`() {
        val original = makeReview(rating = 6.0)
        val updated = original.copy(rating = 9.0, content = "Even better on rewatch")

        assertEquals(9.0, updated.rating!!, 0.001)
        assertEquals("Even better on rewatch", updated.content)
        // Unchanged fields
        assertEquals(original.id, updated.id)
        assertEquals(original.userId, updated.userId)
        assertEquals(original.tmdbId, updated.tmdbId)
    }

    // ── PagingState ───────────────────────────────────────────────

    @Test
    fun `PagingState default values are correct`() {
        val state = PagingState<Review>()
        assertTrue(state.items.isEmpty())
        assertEquals(1, state.currentPage)
        assertEquals(false, state.isLoadingMore)
        assertEquals(true, state.hasMore)
        assertEquals(false, state.isRefreshing)
        assertEquals(null, state.error)
    }

    @Test
    fun `PagingState copy preserves unchanged fields`() {
        val base = PagingState(items = listOf(makeReview(7.0)), currentPage = 2)
        val loading = base.copy(isLoadingMore = true)

        assertEquals(base.items, loading.items)
        assertEquals(2, loading.currentPage)
        assertEquals(true, loading.isLoadingMore)
    }
}
