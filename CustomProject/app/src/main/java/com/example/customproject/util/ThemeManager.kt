package com.example.customproject.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val themeKey = intPreferencesKey(Constants.PREF_THEME)

    val themeFlow: Flow<Int> = dataStore.data.map { prefs ->
        prefs[themeKey] ?: Constants.THEME_SYSTEM
    }

    suspend fun setTheme(mode: Int) {
        dataStore.edit { prefs -> prefs[themeKey] = mode }
        applyTheme(mode)
    }

    fun applyTheme(mode: Int) {
        val nightMode = when (mode) {
            Constants.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            Constants.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
