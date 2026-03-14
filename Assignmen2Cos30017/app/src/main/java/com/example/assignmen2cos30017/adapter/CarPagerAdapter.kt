package com.example.assignmen2cos30017.adapter

import android.animation.AnimatorInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.assignmen2cos30017.R
import com.example.assignmen2cos30017.model.Car
import java.util.Locale

class CarPagerAdapter(
    private var cars: MutableList<Car>,
    private val onRentClick: (Car) -> Unit,
    private val onNextClick: () -> Unit,
    private val onFavouriteClick: (Car, ImageButton) -> Unit,
    private val onLongPressImage: (Car, ImageButton) -> Unit
) : RecyclerView.Adapter<CarPagerAdapter.CarViewHolder>() {

    inner class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView = itemView.findViewById<androidx.cardview.widget.CardView>(R.id.cardViewCar)
        val imgCar = itemView.findViewById<ImageView>(R.id.imgCar)
        val btnFavourite = itemView.findViewById<ImageButton>(R.id.btnFavourite)
        val tvCarName = itemView.findViewById<TextView>(R.id.tvCarName)
        val tvPrice = itemView.findViewById<TextView>(R.id.tvPrice)
        val tvModel = itemView.findViewById<TextView>(R.id.tvModel)
        val tvYear = itemView.findViewById<TextView>(R.id.tvYear)
        val tvKm = itemView.findViewById<TextView>(R.id.tvKm)
        val ratingBar = itemView.findViewById<RatingBar>(R.id.ratingBar)
        val btnRent = itemView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnRent)
        val btnNext = itemView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnNext)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_car_card, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]

        // Set shared element transition name per car
        holder.imgCar.transitionName = "car_image_${car.id}"

        holder.imgCar.setImageResource(car.imageResId)
        holder.tvCarName.text = car.name
        holder.tvPrice.text = String.format(Locale.getDefault(), "%d credits/\nday", car.dailyCost)
        holder.tvModel.text = "Model: ${car.model}"
        holder.tvYear.text = "Year: ${car.year}"
        holder.tvKm.text = "Kilometres: ${String.format(Locale.getDefault(), "%,d", car.kilometres)} km"
        holder.ratingBar.rating = car.rating

        // Favourite state
        updateFavouriteButton(holder.btnFavourite, car.isFavourite)

        // Rent button state when booked
        if (car.isBooked) {
            holder.btnRent.text = "Booked"
            holder.btnRent.isEnabled = false
            holder.btnRent.alpha = 0.5f
        } else {
            holder.btnRent.text = holder.itemView.context.getString(R.string.btn_rent)
            holder.btnRent.isEnabled = true
            holder.btnRent.alpha = 1.0f
        }

        // Card press animation
        holder.cardView.setOnClickListener { view ->
            val animator = AnimatorInflater.loadAnimator(view.context, R.animator.card_press)
            animator.setTarget(view)
            animator.start()
        }

        // Long-press on image to toggle favourite
        holder.imgCar.setOnLongClickListener {
            onLongPressImage(car, holder.btnFavourite)
            true
        }

        holder.btnFavourite.setOnClickListener {
            onFavouriteClick(car, holder.btnFavourite)
        }

        holder.btnRent.setOnClickListener {
            if (!car.isBooked) {
                onRentClick(car)
            }
        }

        holder.btnNext.setOnClickListener {
            onNextClick()
        }
    }

    override fun getItemCount(): Int = cars.size

    fun updateList(newList: List<Car>) {
        cars.clear()
        cars.addAll(newList)
        notifyDataSetChanged()
        // notifyDataSetChanged can leave ViewPager2 blank; requestLayout forces a redraw
        recyclerView?.post { recyclerView?.requestLayout() }
    }

    // Holds a reference to the internal RecyclerView so we can post layout fixes
    private var recyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    fun updateCarState(updatedCar: Car) {
        val idx = cars.indexOfFirst { it.id == updatedCar.id }
        if (idx >= 0) {
            cars[idx] = updatedCar
            notifyItemChanged(idx)
        }
    }

    fun getCarAt(position: Int): Car? = cars.getOrNull(position)

    private fun updateFavouriteButton(btn: ImageButton, isFavourite: Boolean) {
        if (isFavourite) {
            btn.setImageResource(R.drawable.ic_favorite)
            btn.setColorFilter(btn.context.getColor(R.color.favourite_active))
        } else {
            btn.setImageResource(R.drawable.ic_favorite_border)
            btn.setColorFilter(btn.context.getColor(R.color.white))
        }
    }
}
