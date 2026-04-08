package com.example.customproject.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.customproject.data.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val supabase: SupabaseClient
) : ViewModel() {

    // Login + session state
    private val _authState = MutableStateFlow<UiState<Boolean>>(UiState.Loading)
    val authState: StateFlow<UiState<Boolean>> = _authState

    // Sign-up result — Success carries the email for OTP verification
    private val _signUpState = MutableStateFlow<UiState<String>?>(null)
    val signUpState: StateFlow<UiState<String>?> = _signUpState

    // OTP verification result
    private val _otpState = MutableStateFlow<UiState<Boolean>?>(null)
    val otpState: StateFlow<UiState<Boolean>?> = _otpState

    fun checkSession() {
        viewModelScope.launch {
            val session = supabase.auth.currentSessionOrNull()
            _authState.value = UiState.Success(session != null)
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                _authState.value = UiState.Success(true)
            } catch (e: Exception) {
                _authState.value = UiState.Error(e.message ?: "Sign in failed")
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _signUpState.value = UiState.Loading
            try {
                supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                // Account created — Supabase sends OTP to email
                _signUpState.value = UiState.Success(email)
            } catch (e: Exception) {
                _signUpState.value = UiState.Error(e.message ?: "Sign up failed")
            }
        }
    }

    fun verifyOtp(email: String, token: String) {
        viewModelScope.launch {
            _otpState.value = UiState.Loading
            try {
                supabase.auth.verifyEmailOtp(
                    type = OtpType.Email.SIGNUP,
                    email = email,
                    token = token
                )
                supabase.auth.signOut()
                _otpState.value = UiState.Success(true)
            } catch (e: Exception) {
                _otpState.value = UiState.Error(e.message ?: "Invalid OTP")
            }
        }
    }

    fun resendOtp(email: String) {
        viewModelScope.launch {
            try {
                supabase.auth.resendEmail(OtpType.Email.SIGNUP, email)
            } catch (_: Exception) {}
        }
    }

    fun clearSignUpState() { _signUpState.value = null }
    fun clearOtpState() { _otpState.value = null }

    fun signOut() {
        viewModelScope.launch {
            try {
                supabase.auth.signOut()
                _authState.value = UiState.Success(false)
            } catch (e: Exception) {
                _authState.value = UiState.Error(e.message ?: "Sign out failed")
            }
        }
    }

    fun getCurrentUserId(): String? = supabase.auth.currentUserOrNull()?.id
}
