package com.example.customproject.data.repository

import com.example.customproject.data.remote.supabase.dto.WatchlistDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchlistRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    suspend fun addToWatchlist(userId: String, tmdbId: Int) {
        val dto = WatchlistDto(userId = userId, tmdbId = tmdbId, mediaType = "movie")
        supabase.from("watchlist").insert(dto)
    }

    suspend fun removeFromWatchlist(userId: String, tmdbId: Int) {
        supabase.from("watchlist").delete {
            filter {
                eq("user_id", userId)
                eq("tmdb_id", tmdbId)
            }
        }
    }

    suspend fun isInWatchlist(userId: String, tmdbId: Int): Boolean {
        val result = supabase.from("watchlist")
            .select {
                filter {
                    eq("user_id", userId)
                    eq("tmdb_id", tmdbId)
                }
            }
            .decodeList<WatchlistDto>()
        return result.isNotEmpty()
    }
}
