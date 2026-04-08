package com.example.customproject.di

import com.example.customproject.BuildConfig
import com.example.customproject.data.remote.tmdb.TmdbApiService
import com.example.customproject.data.remote.tmdb.TmdbAuthInterceptor
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

// Adds cache headers to make trending/now_playing cacheable for 1 hour; movie detail for 24 hours
private class CacheControlInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val maxAge = when {
            request.url.pathSegments.contains("trending") ||
            request.url.pathSegments.contains("now_playing") -> 3600
            request.url.pathSegments.size >= 2 &&
            request.url.pathSegments[0] == "movie" -> 86400
            else -> 600
        }
        return response.newBuilder()
            .header("Cache-Control", "public, max-age=$maxAge")
            .build()
    }
}

// Returns a cached response when offline (up to 7 days stale)
private class OfflineFallbackInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            chain.proceed(chain.request())
        } catch (e: Exception) {
            val offlineRequest = chain.request().newBuilder()
                .header("Cache-Control", "public, only-if-cached, max-stale=${60 * 60 * 24 * 7}")
                .build()
            chain.proceed(offlineRequest)
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }
        // 50 MB disk cache for TMDB API responses
        val cache = Cache(context.cacheDir, 50L * 1024L * 1024L)
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(OfflineFallbackInterceptor())
            .addInterceptor(TmdbAuthInterceptor(BuildConfig.TMDB_ACCESS_TOKEN))
            .addNetworkInterceptor(CacheControlInterceptor())
            .addNetworkInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideTmdbApiService(retrofit: Retrofit): TmdbApiService =
        retrofit.create(TmdbApiService::class.java)

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient =
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
        }
}
