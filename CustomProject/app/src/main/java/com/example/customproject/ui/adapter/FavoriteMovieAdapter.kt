package com.example.customproject.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.customproject.data.model.Movie
import com.example.customproject.databinding.ItemFavoriteMovieBinding
import com.example.customproject.databinding.ItemLoadingFooterBinding
import com.example.customproject.util.gone
import com.example.customproject.util.loadImage

private const val TYPE_ITEM = 0
private const val TYPE_LOADING = 1

class FavoriteMovieAdapter(
    private val onMovieClick: (Movie) -> Unit,
    private val onPlayClick: (Movie) -> Unit
) : ListAdapter<Movie?, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    override fun getItemViewType(position: Int): Int =
        if (getItem(position) == null) TYPE_LOADING else TYPE_ITEM

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == TYPE_ITEM) {
            MovieViewHolder(
                ItemFavoriteMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        } else {
            LoadingViewHolder(
                ItemLoadingFooterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MovieViewHolder) getItem(position)?.let { holder.bind(it) }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is MovieViewHolder) Glide.with(holder.itemView).clear(holder.binding.ivPoster)
    }

    fun submitPagedList(movies: List<Movie>, showLoading: Boolean) {
        val list: List<Movie?> = if (showLoading) movies + listOf(null) else movies
        submitList(list)
    }

    inner class MovieViewHolder(val binding: ItemFavoriteMovieBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: Movie) {
            binding.ivPoster.loadImage(movie.posterPath)
            binding.tvTitle.text = movie.title
            binding.tvYear.text = movie.releaseYear
            binding.tvDuration.text = movie.formattedRuntime
            binding.tvGenre.text = movie.genres.take(1).joinToString()
            binding.tvRating.text = String.format("%.1f", movie.voteAverage)
            binding.ratingBar.rating = movie.ratingOutOf5
            binding.tvDirector.gone()
            binding.btnPlay.setOnClickListener { onPlayClick(movie) }
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
