package com.example.customproject.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.customproject.data.model.Cast
import com.example.customproject.databinding.ItemCastBinding
import com.example.customproject.util.loadCircleImage

class CastAdapter : ListAdapter<Cast, CastAdapter.CastViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CastViewHolder(
            ItemCastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: CastViewHolder, position: Int) =
        holder.bind(getItem(position))

    override fun onViewRecycled(holder: CastViewHolder) {
        super.onViewRecycled(holder)
        Glide.with(holder.itemView).clear(holder.binding.ivAvatar)
    }

    inner class CastViewHolder(val binding: ItemCastBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cast: Cast) {
            binding.ivAvatar.loadCircleImage(cast.profilePath)
            binding.tvName.text = cast.name
            binding.tvCharacter.text = cast.character
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Cast>() {
            override fun areItemsTheSame(old: Cast, new: Cast) = old.id == new.id
            override fun areContentsTheSame(old: Cast, new: Cast) = old == new
        }
    }
}
