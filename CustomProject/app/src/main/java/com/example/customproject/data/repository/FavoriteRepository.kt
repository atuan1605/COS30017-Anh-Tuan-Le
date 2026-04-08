package com.example.customproject.data.repository

import com.example.customproject.data.remote.supabase.dto.FavoriteDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    suspend fun getFavorites(userId: String, page: Int, pageSize: Int = 20): List<FavoriteDto> {
        val from = ((page - 1) * pageSize).toLong()
        val to = (from + pageSize - 1)
        return supabase.from("favorites")
            .select {
                filter { eq("user_id", userId) }
                range(from, to)
                order("created_at", order = Order.DESCENDING)
            }
            .decodeList()
    }

    suspend fun addFavorite(userId: String, tmdbId: Int): FavoriteDto {
        val dto = FavoriteDto(userId = userId, tmdbId = tmdbId, mediaType = "movie")
        supabase.from("favorites").insert(dto)
        return dto
    }

    suspend fun removeFavorite(userId: String, tmdbId: Int) {
        supabase.from("favorites").delete {
            filter {
                eq("user_id", userId)
                eq("tmdb_id", tmdbId)
            }
        }
    }

    suspend fun isFavorite(userId: String, tmdbId: Int): Boolean {
        val result = supabase.from("favorites")
            .select {
                filter {
                    eq("user_id", userId)
                    eq("tmdb_id", tmdbId)
                }
            }
            .decodeList<FavoriteDto>()
        return result.isNotEmpty()
    }
}
