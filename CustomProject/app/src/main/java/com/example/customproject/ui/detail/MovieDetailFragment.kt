package com.example.customproject.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.customproject.data.model.UiState
import com.example.customproject.databinding.FragmentMovieDetailBinding
import com.example.customproject.ui.adapter.CastAdapter
import com.example.customproject.ui.adapter.ReviewAdapter
import com.example.customproject.ui.auth.AuthViewModel
import com.example.customproject.ui.trailer.TrailerActivity
import com.example.customproject.util.gone
import com.example.customproject.util.loadImage
import com.example.customproject.util.showToast
import com.example.customproject.util.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MovieDetailFragment : Fragment() {

    private var _binding: FragmentMovieDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MovieDetailViewModel by viewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    private lateinit var castAdapter: CastAdapter
    private lateinit var reviewAdapter: ReviewAdapter

    private var movieId: Int = -1
    private var isExpanded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        movieId = arguments?.getInt("tmdb_id", -1) ?: -1
        if (movieId == -1) {
            showToast("Invalid movie")
            findNavController().navigateUp()
            return
        }
        setupRecyclerViews()
        setupScrollListener()
        observeMovieDetail()
        observeCast()
        observeReviews()
        observeFavorite()
        observeWatchlist()
        observeReviewSubmit()
        observeTrailerEvent()
        viewModel.loadMovie(movieId, authViewModel.getCurrentUserId())

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.tvExpandOverview.setOnClickListener { toggleOverview() }
        applyStatusBarInsets()
    }

    private fun applyStatusBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val margin16 = (16 * resources.displayMetrics.density).toInt()
            val topMargin = statusBarHeight + margin16
            (binding.btnBack.layoutParams as FrameLayout.LayoutParams).topMargin = topMargin
            binding.btnBack.requestLayout()
            (binding.chipMediaType.layoutParams as FrameLayout.LayoutParams).topMargin = topMargin
            binding.chipMediaType.requestLayout()
            insets
        }
    }

    private fun setupRecyclerViews() {
        castAdapter = CastAdapter()
        binding.rvCast.apply {
            adapter = castAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

        reviewAdapter = ReviewAdapter()
        // isNestedScrollingEnabled = false prevents scroll conflicts inside NestedScrollView
        binding.rvReviews.apply {
            adapter = reviewAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }
    }

    // Detect near-bottom of NestedScrollView to load more reviews (10/page)
    private fun setupScrollListener() {
        binding.nestedScrollView.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { sv, _, scrollY, _, _ ->
                val totalHeight = sv.getChildAt(0).measuredHeight
                val visibleHeight = sv.measuredHeight
                if (scrollY >= totalHeight - visibleHeight - 300) {
                    viewModel.loadMoreReviews(movieId)
                }
            }
        )
    }

    private fun observeMovieDetail() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.movieState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.progressBar.visible()
                            binding.contentGroup.gone()
                        }
                        is UiState.Success -> {
                            binding.progressBar.gone()
                            binding.contentGroup.visible()
                            val movie = state.data
                            binding.ivBackdrop.loadImage(movie.backdropPath)
                            binding.ivPoster.loadImage(movie.posterPath)
                            binding.tvTitle.text = movie.title
                            binding.tvYear.text = movie.releaseYear
                            binding.tvDuration.text = movie.formattedRuntime
                            binding.tvGenre.text = movie.genres.take(1).joinToString()
                            binding.tvRating.text = String.format("%.1f", movie.voteAverage)
                            binding.ratingBar.rating = movie.ratingOutOf5
                            binding.tvOverview.text = movie.overview
                            setupActionButtons(movie.id)
                        }
                        is UiState.Error -> {
                            binding.progressBar.gone()
                            showToast(state.message)
                        }
                    }
                }
            }
        }
    }

    private fun setupActionButtons(movieId: Int) {
        binding.btnWatchNow.setOnClickListener {
            viewModel.playTrailer(movieId)
        }
        val userId = authViewModel.getCurrentUserId()
        binding.btnFavorite.setOnClickListener {
            if (userId != null) viewModel.toggleFavorite(userId, movieId)
            else showToast("Please log in")
        }
        binding.btnWatchlist.setOnClickListener {
            if (userId != null) viewModel.toggleWatchlist(userId, movieId)
            else showToast("Please log in")
        }
        binding.btnSubmitReview.setOnClickListener {
            val rating = binding.ratingBarInput.rating.toDouble() * 2
            val content = binding.etReview.text.toString().trim()
            if (userId == null) { showToast("Please log in"); return@setOnClickListener }
            if (content.isBlank()) { showToast("Please write a review"); return@setOnClickListener }
            viewModel.submitReview(userId, movieId, rating, content)
        }
    }

    private fun observeCast() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.castState.collect { cast ->
                    castAdapter.submitList(cast)
                    binding.castSection.visibility = if (cast.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    private fun observeReviews() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reviewsState.collect { state ->
                    reviewAdapter.submitPagedList(state.items, state.isLoadingMore)
                    if (state.items.isNotEmpty()) {
                        val avgRating = state.items.mapNotNull { it.rating }.average()
                        binding.tvAverageRating.text = String.format("%.1f", avgRating)
                    }
                }
            }
        }
    }

    private fun observeFavorite() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isFavorite.collect { isFav ->
                    binding.btnFavorite.isSelected = isFav
                }
            }
        }
    }

    private fun observeWatchlist() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isInWatchlist.collect { inWatchlist ->
                    binding.btnWatchlist.isSelected = inWatchlist
                }
            }
        }
    }

    private fun observeReviewSubmit() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reviewSubmitState.collect { state ->
                    when (state) {
                        is UiState.Loading -> binding.btnSubmitReview.isEnabled = false
                        is UiState.Success -> {
                            binding.btnSubmitReview.isEnabled = true
                            binding.etReview.setText("")
                            binding.ratingBarInput.rating = 0f
                            showToast("Review submitted!")
                            viewModel.clearReviewSubmitState()
                        }
                        is UiState.Error -> {
                            binding.btnSubmitReview.isEnabled = true
                            showToast(state.message)
                            viewModel.clearReviewSubmitState()
                        }
                        null -> binding.btnSubmitReview.isEnabled = true
                    }
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

    private fun toggleOverview() {
        isExpanded = !isExpanded
        binding.tvOverview.maxLines = if (isExpanded) Int.MAX_VALUE else 4
        binding.tvExpandOverview.text = if (isExpanded) "Show less" else "Read more"
    }

    override fun onDestroyView() {
        binding.nestedScrollView.setOnScrollChangeListener(null as NestedScrollView.OnScrollChangeListener?)
        binding.rvCast.adapter = null
        binding.rvReviews.adapter = null
        super.onDestroyView()
        _binding = null
    }
}
