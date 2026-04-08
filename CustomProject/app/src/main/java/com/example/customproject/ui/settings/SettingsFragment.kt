package com.example.customproject.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.customproject.R
import com.example.customproject.data.model.UiState
import com.example.customproject.databinding.FragmentSettingsBinding
import com.example.customproject.ui.auth.AuthViewModel
import com.example.customproject.util.Constants
import com.example.customproject.util.loadCircleImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.updatePadding(top = statusBar)
            insets
        }
        observeProfile()
        observeTheme()
        observeThemeUpdated()
        observeSignOut()
        setupThemeButtons()
        setupLogout()
        authViewModel.getCurrentUserId()?.let { viewModel.loadProfile(it) }
    }

    private fun observeProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.profileState.collect { state ->
                    if (state is UiState.Success) {
                        val profile = state.data
                        binding.ivAvatar.loadCircleImage(profile.avatarUrl)
                        binding.tvName.text = profile.fullName ?: profile.username ?: "User"
                        binding.tvEmail.text = profile.updatedAt ?: ""
                    }
                }
            }
        }
    }

    private fun observeTheme() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.themeFlow.collect { mode ->
                    binding.radioSystem.isChecked = mode == Constants.THEME_SYSTEM
                    binding.radioLight.isChecked = mode == Constants.THEME_LIGHT
                    binding.radioDark.isChecked = mode == Constants.THEME_DARK
                }
            }
        }
    }

    private fun observeThemeUpdated() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.themeUpdated.collect {
                    requireActivity().recreate()
                }
            }
        }
    }

    private fun setupThemeButtons() {
        binding.cardThemeSystem.setOnClickListener { viewModel.setTheme(Constants.THEME_SYSTEM) }
        binding.cardThemeLight.setOnClickListener { viewModel.setTheme(Constants.THEME_LIGHT) }
        binding.cardThemeDark.setOnClickListener { viewModel.setTheme(Constants.THEME_DARK) }
    }

    private fun observeSignOut() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signOutEvent.collect {
                    findNavController().navigate(
                        R.id.action_settingsFragment_to_loginFragment,
                        null,
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.nav_graph, true)
                            .build()
                    )
                }
            }
        }
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            viewModel.signOut()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
