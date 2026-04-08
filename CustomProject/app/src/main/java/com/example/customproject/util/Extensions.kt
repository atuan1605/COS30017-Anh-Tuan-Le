package com.example.customproject.util

import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.customproject.R

fun ImageView.loadImage(url: String?, placeholder: Int = R.drawable.ic_movie_placeholder) {
    Glide.with(this)
        .load(url)
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        .placeholder(placeholder)
        .error(placeholder)
        .thumbnail(0.25f)
        .into(this)
}

fun ImageView.loadCircleImage(url: String?) {
    Glide.with(this)
        .load(url)
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        .circleCrop()
        .placeholder(R.drawable.ic_person_placeholder)
        .error(R.drawable.ic_person_placeholder)
        .into(this)
}

fun View.visible() { visibility = View.VISIBLE }
fun View.gone() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

fun Fragment.showToast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun String.toDisplayDate(): String {
    return try {
        if (length >= 10) substring(0, 10) else this
    } catch (e: Exception) {
        this
    }
}
