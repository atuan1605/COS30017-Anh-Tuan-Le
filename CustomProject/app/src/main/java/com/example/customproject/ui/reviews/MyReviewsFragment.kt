package com.example.customproject.ui.reviews

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
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
import com.example.customproject.data.model.Review
import com.example.customproject.databinding.DialogEditReviewBinding
import com.example.customproject.databinding.FragmentMyReviewsBinding
import com.example.customproject.ui.adapter.MyReviewAdapter
import com.example.customproject.ui.auth.AuthViewModel
import com.example.customproject.util.InfiniteScrollListener
import com.example.customproject.util.gone
import com.example.customproject.util.showToast
import com.example.customproject.util.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyReviewsFragment : Fragment() {

    private var _binding: FragmentMyReviewsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyReviewsViewModel by viewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    private lateinit var adapter: MyReviewAdapter
    private var scrollListener: InfiniteScrollListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyReviewsBinding.inflate(inflater, container, false)
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
        observeReviews()
        observeCount()
        loadData()
    }

    private fun setupRecyclerView() {
        val userId = authViewModel.getCurrentUserId()
        adapter = MyReviewAdapter(
            onEditClick = { review -> showEditDialog(review) },
            onDeleteClick = { review ->
                if (userId != null) viewModel.deleteReview(userId, review.id)
            },
            onViewClick = { review ->
                val bundle = Bundle().apply { putInt("tmdb_id", review.tmdbId) }
                findNavController().navigate(R.id.action_myReviewsFragment_to_movieDetailFragment, bundle)
            }
        )
        val layoutManager = LinearLayoutManager(requireContext())
        scrollListener = InfiniteScrollListener(layoutManager) {
            userId?.let { viewModel.loadMoreReviews(it) }
        }
        binding.rvReviews.apply {
            this.adapter = this@MyReviewsFragment.adapter
            this.layoutManager = layoutManager
            addOnScrollListener(scrollListener!!)
        }
    }

    private fun observeReviews() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reviewsState.collect { state ->
                    adapter.submitPagedList(state.items, state.isLoadingMore)
                    if (state.isRefreshing) {
                        binding.progressBar.visible()
                    } else {
                        binding.progressBar.gone()
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

    private fun observeCount() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.totalCount.collect { count ->
                    binding.tvReviewCount.text = count.toString()
                }
            }
        }
    }

    private fun showEditDialog(review: Review) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogEditReviewBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.tvTitle.text = "Edit: ${review.movieTitle ?: "Review"}"
        dialogBinding.ratingBar.rating = review.ratingOutOf5
        dialogBinding.etContent.setText(review.content)

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnSave.setOnClickListener {
            val newContent = dialogBinding.etContent.text?.toString()?.trim() ?: ""
            if (newContent.isBlank()) {
                showToast("Review cannot be empty")
                return@setOnClickListener
            }
            // ratingBar is out of 5, Supabase stores out of 10
            val newRating = dialogBinding.ratingBar.rating.toDouble() * 2
            viewModel.updateReview(review.id, newRating, newContent)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun loadData() {
        val userId = authViewModel.getCurrentUserId()
        if (userId != null) {
            viewModel.loadReviews(userId)
        } else {
            showToast("Please log in to view your reviews")
        }
    }

    override fun onDestroyView() {
        scrollListener?.let { binding.rvReviews.removeOnScrollListener(it) }
        scrollListener = null
        binding.rvReviews.adapter = null
        super.onDestroyView()
        _binding = null
    }
}
