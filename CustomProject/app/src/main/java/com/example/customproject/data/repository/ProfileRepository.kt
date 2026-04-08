package com.example.customproject.data.repository

import com.example.customproject.data.model.Profile
import com.example.customproject.data.remote.supabase.dto.ProfileDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    suspend fun getProfile(userId: String): Profile? {
        val dtos = supabase.from("profiles")
            .select {
                filter { eq("id", userId) }
            }
            .decodeList<ProfileDto>()
        return dtos.firstOrNull()?.toDomain()
    }

    suspend fun updateProfile(userId: String, username: String?, fullName: String?, avatarUrl: String?) {
        supabase.from("profiles").update(
            buildJsonObject {
                username?.let { put("username", it) }
                fullName?.let { put("full_name", it) }
                avatarUrl?.let { put("avatar_url", it) }
            }
        ) {
            filter { eq("id", userId) }
        }
    }

    private fun ProfileDto.toDomain() = Profile(
        id = id,
        username = username,
        fullName = fullName,
        avatarUrl = avatarUrl,
        updatedAt = updatedAt
    )
}
