package com.example.customproject.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.customproject.data.model.Profile
import com.example.customproject.data.model.UiState
import com.example.customproject.data.repository.ProfileRepository
import com.example.customproject.util.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val supabase: SupabaseClient,
    private val profileRepository: ProfileRepository,
    private val themeManager: ThemeManager
) : ViewModel() {

    private val _profileState = MutableStateFlow<UiState<Profile>>(UiState.Loading)
    val profileState: StateFlow<UiState<Profile>> = _profileState

    val themeFlow = themeManager.themeFlow

    private val _themeUpdated = MutableSharedFlow<Unit>()
    val themeUpdated: SharedFlow<Unit> = _themeUpdated.asSharedFlow()

    private val _signOutEvent = MutableSharedFlow<Unit>()
    val signOutEvent: SharedFlow<Unit> = _signOutEvent.asSharedFlow()

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            _profileState.value = UiState.Loading
            try {
                val profile = profileRepository.getProfile(userId)
                _profileState.value = if (profile != null) UiState.Success(profile)
                else UiState.Error("Profile not found")
            } catch (e: Exception) {
                _profileState.value = UiState.Error(e.message ?: "Failed to load profile")
            }
        }
    }

    fun setTheme(mode: Int) {
        viewModelScope.launch {
            themeManager.setTheme(mode)
            _themeUpdated.emit(Unit)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { supabase.auth.signOut() }
            } catch (_: Exception) {}
            _signOutEvent.emit(Unit)
        }
    }
}
