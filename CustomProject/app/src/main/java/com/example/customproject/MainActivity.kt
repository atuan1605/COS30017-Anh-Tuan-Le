package com.example.customproject

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.customproject.databinding.ActivityMainBinding
import com.example.customproject.ui.auth.AuthViewModel
import com.example.customproject.util.ThemeManager
import com.example.customproject.util.gone
import com.example.customproject.util.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    // AuthViewModel is activity-scoped so all fragments can share the same instance
    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        // Hide/show bottom nav based on destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment,
                R.id.favoriteFragment,
                R.id.myReviewsFragment,
                R.id.settingsFragment -> binding.bottomNav.visible()
                else -> binding.bottomNav.gone()
            }
        }

        // Check session once on startup and redirect if not authenticated
        if (savedInstanceState == null) {
            lifecycleScope.launch {
                authViewModel.checkSession()
                authViewModel.authState.collect { state ->
                    // Only act on the first emission (initial session check)
                    if (state is com.example.customproject.data.model.UiState.Success && !state.data) {
                        navController.navigate(
                            R.id.loginFragment,
                            null,
                            androidx.navigation.NavOptions.Builder()
                                .setPopUpTo(R.id.homeFragment, true)
                                .build()
                        )
                    }
                    return@collect
                }
            }
        }
    }

    private fun applyTheme() {
        lifecycleScope.launch {
            themeManager.themeFlow.collect { mode ->
                themeManager.applyTheme(mode)
                return@collect
            }
        }
    }
}
