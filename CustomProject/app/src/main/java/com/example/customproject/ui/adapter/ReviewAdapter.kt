package com.example.customproject.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.customproject.data.model.Review
import com.example.customproject.databinding.ItemLoadingFooterBinding
import com.example.customproject.databinding.ItemReviewBinding
import com.example.customproject.util.loadCircleImage
import com.example.customproject.util.toDisplayDate

private const val TYPE_ITEM = 0
private const val TYPE_LOADING = 1

class ReviewAdapter : ListAdapter<Review?, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    override fun getItemViewType(position: Int): Int =
        if (getItem(position) == null) TYPE_LOADING else TYPE_ITEM

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == TYPE_ITEM) {
            ReviewViewHolder(
                ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        } else {
            LoadingViewHolder(
                ItemLoadingFooterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ReviewViewHolder) getItem(position)?.let { holder.bind(it) }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is ReviewViewHolder) Glide.with(holder.itemView).clear(holder.binding.ivAvatar)
    }

    fun submitPagedList(reviews: List<Review>, showLoading: Boolean) {
        val list: List<Review?> = if (showLoading) reviews + listOf(null) else reviews
        submitList(list)
    }

    inner class ReviewViewHolder(val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            binding.tvUsername.text = review.username ?: "Anonymous"
            binding.tvDate.text = review.createdAt.toDisplayDate()
            binding.tvContent.text = review.content
            binding.tvRatingLabel.text = review.ratingLabel
            binding.ratingBar.rating = review.ratingOutOf5
            binding.ivAvatar.loadCircleImage(review.avatarUrl)
        }
    }

    class LoadingViewHolder(binding: ItemLoadingFooterBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Review?>() {
            override fun areItemsTheSame(old: Review, new: Review) = old.id == new.id
            override fun areContentsTheSame(old: Review, new: Review) = old == new
        }
    }
}
