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
import com.example.customproject.databinding.FragmentLoginBinding
import com.example.customproject.util.gone
import com.example.customproject.util.showToast
import com.example.customproject.util.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    // ViewBinding nulled in onDestroyView to prevent memory leak
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // Use activityViewModels so MainActivity can also observe auth state
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeAuthState()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (email.isBlank() || password.isBlank()) {
                showToast("Please enter email and password")
                return@setOnClickListener
            }
            authViewModel.signIn(email, password)
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun observeAuthState() {
        // Use viewLifecycleOwner to avoid leak after view is destroyed
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        binding.progressBar.visible()
                        binding.btnLogin.isEnabled = false
                    }
                    is UiState.Success -> {
                        binding.progressBar.gone()
                        binding.btnLogin.isEnabled = true
                        if (state.data) {
                            findNavController().navigate(
                                R.id.action_loginFragment_to_homeFragment,
                                null,
                                androidx.navigation.NavOptions.Builder()
                                    .setPopUpTo(R.id.loginFragment, true)
                                    .build()
                            )
                        }
                    }
                    is UiState.Error -> {
                        binding.progressBar.gone()
                        binding.btnLogin.isEnabled = true
                        showToast(state.message)
                    }
                }
            }
        }
    }

    // Null binding here to release the View hierarchy reference — prevents memory leak
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
