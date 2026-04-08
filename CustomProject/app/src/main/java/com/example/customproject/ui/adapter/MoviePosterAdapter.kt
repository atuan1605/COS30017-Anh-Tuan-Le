package com.example.customproject.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.customproject.data.model.Movie
import com.example.customproject.databinding.ItemMovieGridBinding
import com.example.customproject.databinding.ItemMoviePosterBinding
import com.example.customproject.databinding.ItemLoadingFooterBinding
import com.example.customproject.util.loadImage

private const val TYPE_ITEM = 0
private const val TYPE_LOADING = 1

class MoviePosterAdapter(
    private val onMovieClick: (Movie) -> Unit,
    private val useGridLayout: Boolean = false
) : ListAdapter<Movie?, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    override fun getItemViewType(position: Int): Int =
        if (getItem(position) == null) TYPE_LOADING else TYPE_ITEM

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ITEM) {
            if (useGridLayout) {
                GridViewHolder(ItemMovieGridBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            } else {
                MovieViewHolder(ItemMoviePosterBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
        } else {
            LoadingViewHolder(ItemLoadingFooterBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let { movie ->
            when (holder) {
                is MovieViewHolder -> holder.bind(movie)
                is GridViewHolder -> holder.bind(movie)
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        when (holder) {
            is MovieViewHolder -> Glide.with(holder.itemView).clear(holder.binding.ivPoster)
            is GridViewHolder -> Glide.with(holder.itemView).clear(holder.binding.ivPoster)
        }
    }

    fun isLoadingItem(position: Int) = getItemViewType(position) == TYPE_LOADING

    fun submitPagedList(movies: List<Movie>, showLoading: Boolean) {
        val list: List<Movie?> = if (showLoading) movies + listOf(null) else movies
        submitList(list)
    }

    inner class MovieViewHolder(val binding: ItemMoviePosterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie) {
            binding.ivPoster.loadImage(movie.posterPath)
            binding.tvTitle.text = movie.title
            binding.root.setOnClickListener { onMovieClick(movie) }
        }
    }

    inner class GridViewHolder(val binding: ItemMovieGridBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie) {
            binding.ivPoster.loadImage(movie.posterPath)
            binding.tvTitle.text = movie.title
            binding.root.setOnClickListener { onMovieClick(movie) }
        }
    }

    class LoadingViewHolder(binding: ItemLoadingFooterBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Movie?>() {
            override fun areItemsTheSame(old: Movie, new: Movie) = old.id == new.id
            override fun areContentsTheSame(old: Movie, new: Movie) = old == new
        }
    }
}
