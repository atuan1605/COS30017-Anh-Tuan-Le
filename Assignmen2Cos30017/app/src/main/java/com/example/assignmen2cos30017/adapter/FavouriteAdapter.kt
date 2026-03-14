package com.example.assignmen2cos30017.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.assignmen2cos30017.R
import com.example.assignmen2cos30017.model.Car
import java.util.Locale

class FavouriteAdapter(
    private val cars: MutableList<Car>,
    private val onFavouriteToggle: (Car, ImageButton) -> Unit,
    private val onCardClick: (Car) -> Unit
) : RecyclerView.Adapter<FavouriteAdapter.FavViewHolder>() {

    inner class FavViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCar = itemView.findViewById<ImageView>(R.id.imgFavCar)
        val tvName = itemView.findViewById<TextView>(R.id.tvFavCarName)
        val tvModel = itemView.findViewById<TextView>(R.id.tvFavCarModel)
        val tvPrice = itemView.findViewById<TextView>(R.id.tvFavPrice)
        val btnFavourite = itemView.findViewById<ImageButton>(R.id.btnFavouriteFav)
        val cardView = itemView.findViewById<androidx.cardview.widget.CardView>(R.id.cardFavourite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favourite_card, parent, false)
        return FavViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavViewHolder, position: Int) {
        val car = cars[position]

        holder.imgCar.setImageResource(car.imageResId)
        holder.tvName.text = car.name
        holder.tvModel.text = car.model
        holder.tvPrice.text = String.format(Locale.getDefault(), "%d credits/day", car.dailyCost)

        holder.btnFavourite.setOnClickListener {
            onFavouriteToggle(car, holder.btnFavourite)
        }

        holder.cardView.setOnClickListener {
            onCardClick(car)
        }
    }

    override fun getItemCount(): Int = cars.size

    override fun onViewAttachedToWindow(holder: FavViewHolder) {
        super.onViewAttachedToWindow(holder)
        // Fade-in animation when item appears
        holder.itemView.alpha = 0f
        holder.itemView.animate()
            .alpha(1f)
            .setDuration(400)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
    }

    fun updateList(newList: List<Car>) {
        cars.clear()
        cars.addAll(newList)
        notifyDataSetChanged()
    }
}
