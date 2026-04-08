package com.example.customproject.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.customproject.R
import com.example.customproject.data.model.UiState
import com.example.customproject.databinding.FragmentOtpVerificationBinding
import com.example.customproject.util.gone
import com.example.customproject.util.showToast
import com.example.customproject.util.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OtpVerificationFragment : Fragment() {

    private var _binding: FragmentOtpVerificationBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()

    private lateinit var email: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        email = arguments?.getString("email") ?: run {
            showToast("Missing email")
            findNavController().navigateUp()
            return
        }
        authViewModel.clearOtpState()
        binding.tvSubtitle.text = "We sent a 6-digit code to\n$email"
        setupClickListeners()
        observeOtpState()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnVerify.setOnClickListener {
            val code = binding.etOtp.text.toString().trim()
            if (code.length != 6) {
                showToast("Please enter the 6-digit code")
                return@setOnClickListener
            }
            authViewModel.verifyOtp(email, code)
        }

        binding.tvResend.setOnClickListener {
            authViewModel.resendOtp(email)
            showToast("OTP resent to $email")
        }
    }

    private fun observeOtpState() {
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.otpState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        binding.progressBar.visible()
                        binding.btnVerify.isEnabled = false
                    }
                    is UiState.Success -> {
                        binding.progressBar.gone()
                        binding.btnVerify.isEnabled = true
                        showToast("Account verified! Please log in.")
                        findNavController().navigate(
                            R.id.action_otpFragment_to_loginFragment
                        )
                    }
                    is UiState.Error -> {
                        binding.progressBar.gone()
                        binding.btnVerify.isEnabled = true
                        showToast(state.message)
                    }
                    null -> {
                        binding.progressBar.gone()
                        binding.btnVerify.isEnabled = true
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
