package com.example.customproject.data.repository

import com.example.customproject.data.model.Review
import com.example.customproject.data.remote.supabase.dto.ReviewDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    suspend fun getReviewsForMovie(tmdbId: Int, page: Int, pageSize: Int = 10): List<Review> {
        val from = ((page - 1) * pageSize).toLong()
        val to = (from + pageSize - 1)
        val dtos = supabase.from("reviews")
            .select(columns = Columns.raw("*, profiles(username, avatar_url)")) {
                filter { eq("tmdb_id", tmdbId) }
                range(from, to)
                order("created_at", order = Order.DESCENDING)
            }
            .decodeList<ReviewDto>()
        return dtos.map { it.toDomain() }
    }

    suspend fun getMyReviews(userId: String, page: Int, pageSize: Int = 10): List<Review> {
        val from = ((page - 1) * pageSize).toLong()
        val to = (from + pageSize - 1)
        val dtos = supabase.from("reviews")
            .select(columns = Columns.raw("*, profiles(username, avatar_url)")) {
                filter { eq("user_id", userId) }
                range(from, to)
                order("created_at", order = Order.DESCENDING)
            }
            .decodeList<ReviewDto>()
        return dtos.map { it.toDomain() }
    }

    suspend fun getMyReviewCount(userId: String): Long {
        return supabase.from("reviews")
            .select {
                count(Count.EXACT)
                filter { eq("user_id", userId) }
            }
            .countOrNull() ?: 0L
    }

    suspend fun addReview(userId: String, tmdbId: Int, rating: Double, content: String) {
        val dto = ReviewDto(
            userId = userId,
            tmdbId = tmdbId,
            rating = rating,
            content = content
        )
        supabase.from("reviews").insert(dto)
    }

    suspend fun updateReview(reviewId: Long, rating: Double, content: String) {
        supabase.from("reviews").update(
            buildJsonObject {
                put("rating", rating)
                put("content", content)
            }
        ) {
            filter { eq("id", reviewId) }
        }
    }

    suspend fun deleteReview(reviewId: Long) {
        supabase.from("reviews").delete {
            filter { eq("id", reviewId) }
        }
    }

    suspend fun getUserReviewForMovie(userId: String, tmdbId: Int): Review? {
        val dtos = supabase.from("reviews")
            .select {
                filter {
                    eq("user_id", userId)
                    eq("tmdb_id", tmdbId)
                }
            }
            .decodeList<ReviewDto>()
        return dtos.firstOrNull()?.toDomain()
    }

    private fun ReviewDto.toDomain() = Review(
        id = id,
        userId = userId,
        tmdbId = tmdbId,
        rating = rating,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt,
        username = profiles?.username,
        avatarUrl = profiles?.avatarUrl
    )
}
