package com.example.assignmen2cos30017.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.assignmen2cos30017.R
import com.example.assignmen2cos30017.model.Car
import com.google.android.material.button.MaterialButton
import java.util.Locale

class BookedAdapter(
    private val cars: MutableList<Car>,
    private val onCancelClick: (Car) -> Unit,
    private val onCardClick: (Car) -> Unit
) : RecyclerView.Adapter<BookedAdapter.BookedViewHolder>() {

    inner class BookedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCar = itemView.findViewById<ImageView>(R.id.imgBookedCar)
        val tvName = itemView.findViewById<TextView>(R.id.tvBookedCarName)
        val tvModel = itemView.findViewById<TextView>(R.id.tvBookedCarModel)
        val tvPrice = itemView.findViewById<TextView>(R.id.tvBookedPrice)
        val btnCancel = itemView.findViewById<MaterialButton>(R.id.btnCancelBooking)
        val cardView = itemView.findViewById<androidx.cardview.widget.CardView>(R.id.cardBooked)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booked_card, parent, false)
        return BookedViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookedViewHolder, position: Int) {
        val car = cars[position]

        holder.imgCar.setImageResource(car.imageResId)
        holder.tvName.text = car.name
        holder.tvModel.text = car.model
        holder.tvPrice.text = String.format(Locale.getDefault(), "%d credits/day", car.dailyCost)

        holder.btnCancel.setOnClickListener {
            onCancelClick(car)
        }

        holder.cardView.setOnClickListener {
            onCardClick(car)
        }
    }

    override fun getItemCount(): Int = cars.size

    override fun onViewAttachedToWindow(holder: BookedViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.itemView.alpha = 0f
        holder.itemView.animate()
            .alpha(1f)
            .setDuration(350)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
    }

    fun updateList(newList: List<Car>) {
        cars.clear()
        cars.addAll(newList)
        notifyDataSetChanged()
    }
}
