package com.example.assignmen2cos30017.ui

import android.animation.AnimatorInflater
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.assignmen2cos30017.R
import com.example.assignmen2cos30017.data.CarRepository
import com.example.assignmen2cos30017.databinding.ActivityCarDetailBinding
import com.example.assignmen2cos30017.model.Car
import com.example.assignmen2cos30017.utils.CreditManager
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class CarDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarDetailBinding
    private lateinit var car: Car

    private var selectedDays = 1
    private var totalCost = 0
    private var wasBookedInThisSession = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        car = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("car", Car::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("car")
        } ?: run {
            finish()
            return
        }

        setupToolbar()
        populateCarDetails()
        setupFavouriteButton()
        setupSlider()
        setupInputValidation()
        setupSaveButton()
    }

    // ───────────────────────── Toolbar ────────────────────────────

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = ""

        binding.btnBack.setOnClickListener {
            handleBack()
        }
    }

    // ───────────────────────── Populate Data ──────────────────────

    private fun populateCarDetails() {
        binding.imgCarDetail.setImageResource(car.imageResId)
        binding.imgCarDetail.transitionName = "car_image_${car.id}"
        binding.tvDetailCarName.text = car.name
        binding.ratingBarDetail.rating = car.rating
        binding.tvDetailEngine.text = car.engine
        binding.tvDetailSeats.text = "${car.seats} seats"
        binding.tvDetailColor.text = car.color
        binding.tvDetailMileage.text = car.mileage
        binding.tvDetailTransmission.text = car.transmission
        binding.tvDetailFuel.text = car.fuel
        binding.tvDetailDescription.text = car.description
        binding.tvDetailPrice.text = String.format(Locale.getDefault(), "%d credits/day", car.dailyCost)

        selectedDays = 1
        totalCost = car.dailyCost * selectedDays
        binding.tvDays.text = String.format(Locale.getDefault(), "%d day(s)", selectedDays)
        binding.tvTotal.text = String.format(Locale.getDefault(), "Total: %d credits", totalCost)

        // If car is already booked, show cancel button
        if (car.isBooked) {
            binding.btnSave.text = getString(R.string.btn_cancel_booking)
        } else {
            binding.btnSave.text = getString(R.string.btn_save)
        }
    }

    // ───────────────────────── Favourite ──────────────────────────

    private fun setupFavouriteButton() {
        updateFavouriteIcon()

        binding.btnFavDetail.setOnClickListener {
            CarRepository.toggleFavourite(car.id)
            car = CarRepository.getCarById(car.id) ?: car
            updateFavouriteIcon()

            // Pulse animation
            val animator = AnimatorInflater.loadAnimator(this, R.animator.heart_pulse)
            animator.setTarget(binding.btnFavDetail)
            animator.start()
        }
    }

    private fun updateFavouriteIcon() {
        if (car.isFavourite) {
            binding.btnFavDetail.setImageResource(R.drawable.ic_favorite)
            binding.btnFavDetail.setColorFilter(getColor(R.color.favourite_active))
        } else {
            binding.btnFavDetail.setImageResource(R.drawable.ic_favorite_border)
            binding.btnFavDetail.setColorFilter(getColor(R.color.white))
        }
    }

    // ───────────────────────── Slider ─────────────────────────────

    private fun setupSlider() {
        binding.sliderDays.value = 1f
        binding.sliderDays.addOnChangeListener { _, value, _ ->
            selectedDays = value.toInt()
            totalCost = car.dailyCost * selectedDays
            binding.tvDays.text = String.format(Locale.getDefault(), "%d day(s)", selectedDays)
            binding.tvTotal.text = String.format(Locale.getDefault(), "Total: %d credits", totalCost)
            validateAndUpdateUI()
        }
    }

    // ───────────────────────── Validation ─────────────────────────

    private fun setupInputValidation() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { validateAndUpdateUI() }
        }
        binding.etName.addTextChangedListener(watcher)
        binding.etPhone.addTextChangedListener(watcher)
    }

    private fun validateAndUpdateUI(): Boolean {
        val name = binding.etName.text?.toString()?.trim() ?: ""
        val phone = binding.etPhone.text?.toString()?.trim() ?: ""
        var isValid = true

        // Name validation
        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.error_name_required)
            shakeView(binding.tilName)
            isValid = false
        } else {
            binding.tilName.error = null
        }

        // Phone validation
        if (phone.isEmpty() || phone.length < 7 || !phone.any { it.isDigit() }) {
            if (phone.isNotEmpty()) {
                binding.tilPhone.error = getString(R.string.error_phone_invalid)
                shakeView(binding.tilPhone)
            } else {
                binding.tilPhone.error = getString(R.string.error_name_required)
            }
            isValid = false
        } else {
            binding.tilPhone.error = null
        }

        // Credit validation
        when {
            totalCost > CreditManager.MAX_PER_RENTAL -> {
                showCreditError(getString(R.string.error_exceeds_max))
                isValid = false
            }
            totalCost > CreditManager.balance -> {
                showCreditError(
                    String.format(Locale.getDefault(),
                        getString(R.string.error_insufficient_balance),
                        CreditManager.balance)
                )
                isValid = false
            }
            else -> clearCreditError()
        }

        binding.btnSave.isEnabled = isValid || car.isBooked
        return isValid
    }

    private fun showCreditError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = android.view.View.VISIBLE
        binding.tvTotal.setTextColor(getColor(R.color.error_color))
    }

    private fun clearCreditError() {
        binding.tvError.visibility = android.view.View.GONE
        binding.tvTotal.setTextColor(getColorFromAttr(com.google.android.material.R.attr.colorOnSurface))
    }

    private fun getColorFromAttr(attr: Int): Int {
        val typedArray = obtainStyledAttributes(intArrayOf(attr))
        val color = typedArray.getColor(0, 0)
        typedArray.recycle()
        return color
    }

    private fun shakeView(view: android.view.View) {
        val animator = AnimatorInflater.loadAnimator(this, R.animator.error_shake)
        animator.setTarget(view)
        animator.start()
    }

    // ───────────────────────── Save Button ────────────────────────

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            if (car.isBooked) {
                // Cancel booking
                cancelBooking()
            } else {
                // Confirm booking
                if (validateAndUpdateUI()) {
                    confirmBooking()
                }
            }
        }
    }

    private fun confirmBooking() {
        CreditManager.deduct(totalCost)
        CarRepository.bookCar(car.id)
        car = car.copy(isBooked = true)
        wasBookedInThisSession = true

        // Update button text
        binding.btnSave.text = getString(R.string.btn_cancel_booking)

        // Snackbar confirmation
        Snackbar.make(
            binding.coordinatorDetail,
            String.format(Locale.getDefault(), getString(R.string.booking_confirmed), totalCost),
            Snackbar.LENGTH_LONG
        ).show()

        // Return result
        val result = Intent().apply {
            putExtra("car", car)
            putExtra("totalCost", totalCost)
        }
        setResult(RESULT_OK, result)

        // Close after a short delay to show snackbar
        binding.coordinatorDetail.postDelayed({ finish() }, 1500)
    }

    private fun cancelBooking() {
        if (wasBookedInThisSession) {
            CreditManager.refund(totalCost)
            CarRepository.cancelCar(car.id)
        } else {
            // Cancel pre-existing booking
            CreditManager.refund(car.dailyCost * selectedDays)
            CarRepository.cancelCar(car.id)
        }
        car = car.copy(isBooked = false)

        Toast.makeText(this, getString(R.string.booking_cancelled), Toast.LENGTH_SHORT).show()

        val result = Intent().apply {
            putExtra("car", car)
            putExtra("totalCost", 0)
        }
        setResult(RESULT_CANCELED, result)
        finish()
    }

    // ───────────────────────── Back ───────────────────────────────

    private fun handleBack() {
        if (wasBookedInThisSession && car.isBooked) {
            // Keep as booked, return OK
            val result = Intent().apply {
                putExtra("car", car)
                putExtra("totalCost", totalCost)
            }
            setResult(RESULT_OK, result)
        } else {
            val result = Intent().apply {
                putExtra("car", car)
                putExtra("totalCost", 0)
            }
            setResult(RESULT_CANCELED, result)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, R.anim.fade_in, R.anim.slide_out_down)
            finish()
        } else {
            finish()
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.fade_in, R.anim.slide_out_down)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        handleBack()
    }
}
