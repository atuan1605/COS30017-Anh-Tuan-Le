package com.example.customproject.data.remote.tmdb

import okhttp3.Interceptor
import okhttp3.Response

class TmdbAuthInterceptor(private val accessToken: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("accept", "application/json")
            .build()
        return chain.proceed(request)
    }
}
