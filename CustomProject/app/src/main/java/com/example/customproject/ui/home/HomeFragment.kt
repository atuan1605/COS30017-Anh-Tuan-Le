package com.example.customproject.ui.home

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customproject.R
import com.example.customproject.data.model.UiState
import com.example.customproject.databinding.FragmentHomeBinding
import com.example.customproject.ui.adapter.MoviePosterAdapter
import com.example.customproject.ui.trailer.TrailerActivity
import com.example.customproject.util.gone
import com.example.customproject.util.loadImage
import com.example.customproject.util.showToast
import com.example.customproject.util.visible
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private const val GRID_SPAN = 3
private const val LOAD_MORE_THRESHOLD = GRID_SPAN * 2

private val GENRE_LIST = listOf(
    "All" to null,
    "Action" to 28,
    "Adventure" to 12,
    "Animation" to 16,
    "Comedy" to 35,
    "Crime" to 80,
    "Documentary" to 99,
    "Drama" to 18,
    "Fantasy" to 14,
    "Horror" to 27,
    "Mystery" to 9648,
    "Romance" to 10749,
    "Science Fiction" to 878,
    "Thriller" to 53
)

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var trendingAdapter: MoviePosterAdapter
    private lateinit var searchAdapter: MoviePosterAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyWindowInsets()
        setupTrendingRecyclerView()
        setupSearchRecyclerView()
        setupSearch()
        setupFilterButton()
        setupAppBarListener()
        observeFeatured()
        observeTrending()
        observeSearch()
        observeGenreFilter()
        observeTrailerEvent()
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val dp10 = (10 * resources.displayMetrics.density).toInt()
            binding.searchRow.updatePadding(top = statusBar + dp10)
            binding.heroContainer.updateLayoutParams {
                height = (220 * resources.displayMetrics.density).toInt() + statusBar
            }
            insets
        }
    }

    private fun setupTrendingRecyclerView() {
        trendingAdapter = MoviePosterAdapter(
            onMovieClick = { movie ->
                val bundle = Bundle().apply { putInt("tmdb_id", movie.id) }
                findNavController().navigate(R.id.action_homeFragment_to_movieDetailFragment, bundle)
            },
            useGridLayout = true
        )

        val gridLayoutManager = GridLayoutManager(requireContext(), GRID_SPAN)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int =
                if (trendingAdapter.isLoadingItem(position)) GRID_SPAN else 1
        }

        binding.rvTrending.apply {
            adapter = trendingAdapter
            layoutManager = gridLayoutManager
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 0) checkLoadMore()
                }
            })
        }
    }

    // When all items fit on screen, RecyclerView doesn't scroll so onScrolled never fires.
    // Detect this after each data update and when AppBarLayout finishes collapsing.
    private fun checkLoadMore() {
        val lm = binding.rvTrending.layoutManager as? GridLayoutManager ?: return
        val lastVisible = lm.findLastVisibleItemPosition()
        val total = lm.itemCount
        if (total > 0 && lastVisible >= total - 1 - LOAD_MORE_THRESHOLD) {
            viewModel.loadMoreTrending()
        }
    }

    // When AppBarLayout collapse completes, the RV has more visible area —
    // items that previously needed scrolling now all fit, so re-check.
    private fun setupAppBarListener() {
        binding.appBarLayout.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener { _, _ ->
                if (binding.rvTrending.visibility == View.VISIBLE) {
                    binding.rvTrending.post { checkLoadMore() }
                }
            }
        )
    }

    private fun setupSearchRecyclerView() {
        searchAdapter = MoviePosterAdapter(
            onMovieClick = { movie ->
                val bundle = Bundle().apply { putInt("tmdb_id", movie.id) }
                findNavController().navigate(R.id.action_homeFragment_to_movieDetailFragment, bundle)
            },
            useGridLayout = true
        )
        val gridLayoutManager = GridLayoutManager(requireContext(), GRID_SPAN)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int =
                if (searchAdapter.isLoadingItem(position)) GRID_SPAN else 1
        }
        binding.rvSearch.apply {
            adapter = searchAdapter
            layoutManager = gridLayoutManager
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { editable ->
            val query = editable?.toString() ?: ""
            viewModel.searchQuery.value = query
            if (query.isBlank()) {
                binding.rvSearch.gone()
                binding.rvTrending.visible()
                binding.titleRow.visible()
            } else {
                binding.rvSearch.visible()
                binding.rvTrending.gone()
                binding.titleRow.gone()
            }
        }
    }

    private fun setupFilterButton() {
        binding.btnFilter.setOnClickListener { showFilterDialog() }
    }

    private fun showFilterDialog() {
        val names = GENRE_LIST.map { it.first }.toTypedArray()
        val currentGenreId = viewModel.selectedGenreId.value
        val currentIndex = GENRE_LIST.indexOfFirst { it.second == currentGenreId }.coerceAtLeast(0)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filter by Genre")
            .setSingleChoiceItems(names, currentIndex) { dialog, which ->
                val genreId = GENRE_LIST[which].second
                viewModel.setGenreFilter(genreId)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeFeatured() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.featuredMovie.collect { state ->
                    when (state) {
                        is UiState.Loading -> {}
                        is UiState.Success -> {
                            val movie = state.data
                            binding.ivBackdrop.loadImage(movie.backdropPath)
                            binding.tvMovieTitle.text = movie.title
                            binding.btnPlay.setOnClickListener {
                                viewModel.playTrailer(movie.id)
                            }
                            binding.btnDetail.setOnClickListener {
                                val bundle = Bundle().apply { putInt("tmdb_id", movie.id) }
                                findNavController().navigate(R.id.action_homeFragment_to_movieDetailFragment, bundle)
                            }
                        }
                        is UiState.Error -> {}
                    }
                }
            }
        }
    }

    private fun observeTrending() {
        binding.tvTrendingError.setOnClickListener { viewModel.loadTrending() }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.trendingState.collect { state ->
                    trendingAdapter.submitPagedList(state.items, state.isLoadingMore)
                    binding.trendingProgress.visibility =
                        if (state.isRefreshing) View.VISIBLE else View.GONE
                    if (state.error != null && state.items.isEmpty()) {
                        binding.tvTrendingError.visible()
                        binding.rvTrending.gone()
                    } else if (state.items.isNotEmpty()) {
                        binding.tvTrendingError.gone()
                        binding.rvTrending.visible()
                    }

                    // After list is updated, post a check: if all items fit on screen, auto-load next page
                    if (!state.isRefreshing && !state.isLoadingMore && state.hasMore && state.items.isNotEmpty()) {
                        binding.rvTrending.post { checkLoadMore() }
                    }
                }
            }
        }
    }

    private fun observeSearch() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchState.collect { state ->
                    searchAdapter.submitPagedList(state.items, false)
                }
            }
        }
    }

    private fun observeGenreFilter() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedGenreId.collect { genreId ->
                    val genreName = GENRE_LIST.find { it.second == genreId }?.first
                    binding.tvTrendingTitle.text =
                        if (genreId != null) "$genreName Movies" else "Trending Now"
                    val tintColor = if (genreId != null)
                        requireContext().getColor(R.color.colorAccent)
                    else
                        requireContext().getColor(R.color.colorTextPrimary)
                    binding.btnFilter.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
                }
            }
        }
    }

    private fun observeTrailerEvent() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.trailerEvent.collect { key ->
                    if (key != null) {
                        startActivity(Intent(requireContext(), TrailerActivity::class.java).apply {
                            putExtra(TrailerActivity.EXTRA_YOUTUBE_KEY, key)
                        })
                    } else {
                        showToast("No trailer available")
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        binding.rvTrending.adapter = null
        binding.rvSearch.adapter = null
        super.onDestroyView()
        _binding = null
    }
}
