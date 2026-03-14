package com.example.assignmen2cos30017.ui

import android.animation.AnimatorInflater
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.assignmen2cos30017.R
import com.example.assignmen2cos30017.adapter.BookedAdapter
import com.example.assignmen2cos30017.adapter.CarPagerAdapter
import com.example.assignmen2cos30017.adapter.FavouriteAdapter
import com.example.assignmen2cos30017.data.CarRepository
import com.example.assignmen2cos30017.databinding.ActivityMainBinding
import com.example.assignmen2cos30017.model.Car
import com.example.assignmen2cos30017.utils.CreditManager
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var pagerAdapter: CarPagerAdapter
    private lateinit var favouriteAdapter: FavouriteAdapter
    private lateinit var bookedAdapter: BookedAdapter

    private val dotViews = mutableListOf<ImageView>()

    companion object {
        const val REQUEST_RENT = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupDotIndicators()
        setupFavouritesRecyclerView()
        setupBookedRecyclerView()
        setupSearchView()
        setupSortButton()
        setupDarkModeButton()
        updateBalanceDisplay(CreditManager.balance)
    }

    // ───────────────────────── ViewPager2 ─────────────────────────

    private fun setupViewPager() {
        val cars = CarRepository.getAvailableCars()
        pagerAdapter = CarPagerAdapter(
            cars = cars,
            onRentClick = { car -> navigateToDetail(car) },
            onNextClick = {
                val next = binding.viewPagerCars.currentItem + 1
                binding.viewPagerCars.currentItem = if (next < pagerAdapter.itemCount) next else 0
            },
            onFavouriteClick = { car, btn -> toggleFavourite(car, btn) },
            onLongPressImage = { car, btn -> toggleFavourite(car, btn) }
        )

        binding.viewPagerCars.adapter = pagerAdapter
        binding.viewPagerCars.setPageTransformer(ZoomOutPageTransformer())
        binding.viewPagerCars.offscreenPageLimit = 2

        binding.viewPagerCars.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
            }
        })
    }

    // ───────────────────────── Dot Indicators ─────────────────────

    private fun setupDotIndicators() {
        val count = pagerAdapter.itemCount
        binding.layoutDots.removeAllViews()
        dotViews.clear()

        for (i in 0 until count) {
            val dot = ImageView(this)
            val size = if (i == 0) 10 else 8
            val params = LinearLayout.LayoutParams(
                dpToPx(size), dpToPx(size)
            ).apply { setMargins(dpToPx(4), 0, dpToPx(4), 0) }
            dot.layoutParams = params
            dot.setImageResource(if (i == 0) R.drawable.bg_dot_active else R.drawable.bg_dot_inactive)
            dotViews.add(dot)
            binding.layoutDots.addView(dot)
        }
    }

    private fun updateDots(selected: Int) {
        dotViews.forEachIndexed { index, dot ->
            val size = if (index == selected) 10 else 8
            val params = LinearLayout.LayoutParams(dpToPx(size), dpToPx(size)).apply {
                setMargins(dpToPx(4), 0, dpToPx(4), 0)
            }
            dot.layoutParams = params
            dot.setImageResource(if (index == selected) R.drawable.bg_dot_active else R.drawable.bg_dot_inactive)
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    // ───────────────────────── Favourites ─────────────────────────

    private fun setupFavouritesRecyclerView() {
        val favs = CarRepository.getFavourites().toMutableList()
        favouriteAdapter = FavouriteAdapter(
            cars = favs,
            onFavouriteToggle = { car, btn -> toggleFavourite(car, btn) },
            onCardClick = { car -> navigateToDetail(car) }
        )
        binding.rvFavourites.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvFavourites.adapter = favouriteAdapter
        refreshFavouritesSection()
    }

    private fun refreshFavouritesSection() {
        val favs = CarRepository.getFavourites()
        favouriteAdapter.updateList(favs)
        if (favs.isEmpty()) {
            binding.tvFavouritesHeader.visibility = View.GONE
            binding.rvFavourites.visibility = View.GONE
        } else {
            binding.tvFavouritesHeader.visibility = View.VISIBLE
            binding.rvFavourites.visibility = View.VISIBLE
        }
    }

    private fun setupBookedRecyclerView() {
        bookedAdapter = BookedAdapter(
            cars = CarRepository.getBookedCars().toMutableList(),
            onCancelClick = { car -> cancelBookingFromList(car) },
            onCardClick = { car -> navigateToDetail(car) }
        )
        binding.rvBooked.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvBooked.adapter = bookedAdapter
        refreshBookedSection()
    }

    private fun refreshBookedSection() {
        val booked = CarRepository.getBookedCars()
        bookedAdapter.updateList(booked)
        if (booked.isEmpty()) {
            binding.tvBookedHeader.visibility = View.GONE
            binding.rvBooked.visibility = View.GONE
        } else {
            binding.tvBookedHeader.visibility = View.VISIBLE
            binding.rvBooked.visibility = View.VISIBLE
        }
    }

    private fun cancelBookingFromList(car: Car) {
        // Refund and cancel
        CreditManager.refund(car.dailyCost)
        CarRepository.cancelCar(car.id)
        // Refresh pager with newly available car
        pagerAdapter.updateList(CarRepository.getAvailableCars())
        setupDotIndicators()
        refreshBookedSection()
        refreshFavouritesSection()
        updateBalanceDisplay(CreditManager.balance)
        Toast.makeText(this, getString(R.string.booking_cancelled), Toast.LENGTH_SHORT).show()
    }

    private fun toggleFavourite(car: Car, btn: ImageButton) {
        CarRepository.toggleFavourite(car.id)
        val updatedCar = CarRepository.getCarById(car.id) ?: return
        updateFavouriteButtonIcon(btn, updatedCar.isFavourite)

        // Heart pulse animation
        val animator = AnimatorInflater.loadAnimator(this, R.animator.heart_pulse)
        animator.setTarget(btn)
        animator.start()

        refreshFavouritesSection()
        pagerAdapter.updateCarState(updatedCar)
    }

    private fun updateFavouriteButtonIcon(btn: ImageButton, isFavourite: Boolean) {
        if (isFavourite) {
            btn.setImageResource(R.drawable.ic_favorite)
            btn.setColorFilter(getColor(R.color.favourite_active))
        } else {
            btn.setImageResource(R.drawable.ic_favorite_border)
            btn.setColorFilter(getColor(R.color.favourite_inactive))
        }
    }

    // ───────────────────────── Search ─────────────────────────────

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                performSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                performSearch(newText)
                return true
            }
        })
    }

    private fun performSearch(query: String?) {
        if (query.isNullOrBlank()) {
            val all = CarRepository.getAvailableCars()
            pagerAdapter.updateList(all)
            binding.tvNoResults.visibility = View.GONE
        } else {
            val results = CarRepository.searchCars(query)
            if (results.isEmpty()) {
                binding.tvNoResults.visibility = View.VISIBLE
                pagerAdapter.updateList(mutableListOf())
            } else {
                binding.tvNoResults.visibility = View.GONE
                pagerAdapter.updateList(results.toMutableList())
            }
        }
        setupDotIndicators()
        binding.viewPagerCars.currentItem = 0
    }

    // ───────────────────────── Sort / Filter ──────────────────────

    private fun setupSortButton() {
        binding.btnSort.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menu.add(Menu.NONE, 1, 1, getString(R.string.sort_rating))
            popup.menu.add(Menu.NONE, 2, 2, getString(R.string.sort_year))
            popup.menu.add(Menu.NONE, 3, 3, getString(R.string.sort_cost))

            popup.setOnMenuItemClickListener { item ->
                val sorted: List<Car> = when (item.itemId) {
                    1 -> CarRepository.sortByRatingDesc().filter { !it.isBooked }
                    2 -> CarRepository.sortByYearDesc().filter { !it.isBooked }
                    3 -> CarRepository.sortByCostAsc().filter { !it.isBooked }
                    else -> CarRepository.getAvailableCars()
                }
                pagerAdapter.updateList(sorted.toMutableList())
                setupDotIndicators()
                binding.viewPagerCars.currentItem = 0
                true
            }
            popup.show()
        }
    }

    // ───────────────────────── Dark Mode ──────────────────────────

    private fun setupDarkModeButton() {
        // Set initial label: show the mode we WILL switch TO
        val isDark = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        binding.btnDarkMode.text = getString(
            if (isDark) R.string.btn_light_mode else R.string.btn_dark_mode
        )

        binding.btnDarkMode.setOnClickListener {
            val currentMode = AppCompatDelegate.getDefaultNightMode()
            if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            // Activity will recreate; text is re-set in next onCreate
        }
    }

    // ───────────────────────── Balance ────────────────────────────

    private fun updateBalanceDisplay(newBalance: Int) {
        binding.tvBalance.text = String.format(Locale.getDefault(), "Balance: %d credits", newBalance)
    }

    private fun animateBalance(fromValue: Int, toValue: Int) {
        val animator = ValueAnimator.ofInt(fromValue, toValue)
        animator.duration = 600
        animator.addUpdateListener { anim ->
            val value = anim.animatedValue as Int
            binding.tvBalance.text = String.format(Locale.getDefault(), "Balance: %d credits", value)
        }
        animator.start()
    }

    // ───────────────────────── Navigation ─────────────────────────

    private fun navigateToDetail(car: Car) {
        val intent = Intent(this, CarDetailActivity::class.java)
        intent.putExtra("car", car)
        @Suppress("DEPRECATION")
        startActivityForResult(intent, REQUEST_RENT,
            android.os.Bundle().also {
                val anim = android.app.ActivityOptions.makeCustomAnimation(
                    this,
                    R.anim.slide_in_up,
                    R.anim.fade_out
                )
                it.putAll(anim.toBundle())
            }
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_RENT) {
            val car = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getParcelableExtra("car", Car::class.java)
            } else {
                @Suppress("DEPRECATION")
                data?.getParcelableExtra<Car>("car")
            }
            val totalCost = data?.getIntExtra("totalCost", 0) ?: 0

            when (resultCode) {
                RESULT_OK -> {
                    // Booking confirmed: remove from pager, add to booked list, animate balance
                    pagerAdapter.updateList(CarRepository.getAvailableCars())
                    setupDotIndicators()
                    refreshFavouritesSection()
                    refreshBookedSection()
                    val oldBalance = CreditManager.balance + totalCost
                    animateBalance(oldBalance, CreditManager.balance)
                }
                RESULT_CANCELED -> {
                    // Booking cancelled: add back to pager, remove from booked list
                    pagerAdapter.updateList(CarRepository.getAvailableCars())
                    setupDotIndicators()
                    refreshFavouritesSection()
                    refreshBookedSection()
                    updateBalanceDisplay(CreditManager.balance)
                }
            }
        }
    }
}

/**
 * ZoomOutPageTransformer: Creates a zoom-out animation when swiping between pages.
 */
class ZoomOutPageTransformer : ViewPager2.PageTransformer {
    companion object {
        private const val MIN_SCALE = 0.85f
        private const val MIN_ALPHA = 0.5f
    }

    override fun transformPage(view: View, position: Float) {
        view.apply {
            val pageWidth = width
            val pageHeight = height
            when {
                position < -1 -> alpha = 0f
                position <= 1 -> {
                    val scaleFactor = MIN_SCALE.coerceAtLeast(1 - Math.abs(position))
                    val vertMargin = pageHeight * (1 - scaleFactor) / 2
                    val horizMargin = pageWidth * (1 - scaleFactor) / 2
                    translationX = if (position < 0) {
                        horizMargin - vertMargin / 2
                    } else {
                        -horizMargin + vertMargin / 2
                    }
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                    alpha = (MIN_ALPHA + (((scaleFactor - MIN_SCALE) / (1 - MIN_SCALE)) * (1 - MIN_ALPHA)))
                }
                else -> alpha = 0f
            }
        }
    }
}
