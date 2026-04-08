package com.example.customproject

import android.app.Application
import android.util.LruCache
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MovieApp : Application() {

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_MODERATE) {
            movieLruCache.evictAll()
        }
    }

    companion object {
        val movieLruCache = LruCache<Int, Any>(100)
    }
}
