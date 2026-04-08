package com.example.customproject.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.customproject.R
import com.example.customproject.data.model.UiState
import com.example.customproject.databinding.FragmentRegisterBinding
import com.example.customproject.util.gone
import com.example.customproject.util.showToast
import com.example.customproject.util.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authViewModel.clearSignUpState()
        setupClickListeners()
        observeSignUpState()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirm = binding.etConfirmPassword.text.toString().trim()
            if (email.isBlank() || password.isBlank()) {
                showToast("Please fill in all fields")
                return@setOnClickListener
            }
            if (password != confirm) {
                showToast("Passwords do not match")
                return@setOnClickListener
            }
            authViewModel.signUp(email, password)
        }

        binding.tvLogin.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeSignUpState() {
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.signUpState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        binding.progressBar.visible()
                        binding.btnRegister.isEnabled = false
                    }
                    is UiState.Success -> {
                        binding.progressBar.gone()
                        binding.btnRegister.isEnabled = true
                        // Navigate to OTP verification, passing the email
                        findNavController().navigate(
                            R.id.action_registerFragment_to_otpFragment,
                            bundleOf("email" to state.data)
                        )
                    }
                    is UiState.Error -> {
                        binding.progressBar.gone()
                        binding.btnRegister.isEnabled = true
                        showToast(state.message)
                    }
                    null -> {
                        binding.progressBar.gone()
                        binding.btnRegister.isEnabled = true
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
