package com.example.customproject.ui.favorite

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.customproject.R
import com.example.customproject.databinding.FragmentFavoriteBinding
import com.example.customproject.ui.adapter.FavoriteMovieAdapter
import com.example.customproject.ui.auth.AuthViewModel
import com.example.customproject.util.InfiniteScrollListener
import com.example.customproject.util.gone
import com.example.customproject.util.showToast
import com.example.customproject.util.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoriteViewModel by viewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    private lateinit var adapter: FavoriteMovieAdapter
    private var scrollListener: InfiniteScrollListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.updatePadding(top = statusBar)
            insets
        }
        setupRecyclerView()
        observeFavorites()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = FavoriteMovieAdapter(
            onMovieClick = { movie ->
                val bundle = Bundle().apply { putInt("tmdb_id", movie.id) }
                findNavController().navigate(R.id.action_favoriteFragment_to_movieDetailFragment, bundle)
            },
            onPlayClick = { movie ->
                val bundle = Bundle().apply { putInt("tmdb_id", movie.id) }
                findNavController().navigate(R.id.action_favoriteFragment_to_movieDetailFragment, bundle)
            }
        )
        val layoutManager = LinearLayoutManager(requireContext())
        scrollListener = InfiniteScrollListener(layoutManager) {
            authViewModel.getCurrentUserId()?.let { viewModel.loadMoreFavorites(it) }
        }
        binding.rvFavorites.apply {
            this.adapter = this@FavoriteFragment.adapter
            this.layoutManager = layoutManager
            addOnScrollListener(scrollListener!!)
        }
    }

    private fun observeFavorites() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favoritesState.collect { state ->
                    adapter.submitPagedList(state.items, state.isLoadingMore)
                    if (state.isRefreshing) {
                        binding.progressBar.visible()
                        binding.rvFavorites.gone()
                    } else {
                        binding.progressBar.gone()
                        binding.rvFavorites.visible()
                    }
                    if (state.items.isEmpty() && !state.isRefreshing) {
                        binding.tvEmpty.visible()
                    } else {
                        binding.tvEmpty.gone()
                    }
                    state.error?.let { showToast(it) }
                }
            }
        }
    }

    private fun loadData() {
        val userId = authViewModel.getCurrentUserId()
        if (userId != null) {
            viewModel.loadFavorites(userId)
        } else {
            showToast("Please log in to view favorites")
        }
    }

    override fun onDestroyView() {
        scrollListener?.let { binding.rvFavorites.removeOnScrollListener(it) }
        scrollListener = null
        binding.rvFavorites.adapter = null
        super.onDestroyView()
        _binding = null
    }
}
