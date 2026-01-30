package com.example.gymnasticsapp

import android.animation.ObjectAnimator
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val viewModel: GymnasticsViewModel by viewModels()
    
    lateinit var scoreTxt: TextView;
    lateinit var elementTxt: TextView;
    lateinit var resetBtn: Button;
    lateinit var performBtn: Button;
    lateinit var deductionBtn: Button;
    lateinit var fireworksView: TextView;
    lateinit var emojiIcon: ImageView;
    private var isSadMode = false;
    
    private lateinit var soundPool: SoundPool
    private var soundButtonClick = 0
    private var soundScoreIncrease = 0
    private var soundDeduction = 0
    private var soundComplete = 0
    private var soundsLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("MainActivity", "onCreate: Initializing application")
        scoreTxt = findViewById(R.id.scoreTxt);
        elementTxt = findViewById(R.id.elementTxt);
        resetBtn = findViewById(R.id.resetBtn);
        performBtn = findViewById(R.id.performBtn);
        deductionBtn = findViewById(R.id.deductionBtn);
        fireworksView = findViewById(R.id.fireworksView);
        emojiIcon = findViewById(R.id.emojiIcon);

        resetBtn.setOnClickListener (this);
        performBtn.setOnClickListener(this);
        deductionBtn.setOnClickListener(this);
        
        // Initialize SoundPool
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()
        
        // Load sounds (add sound files to res/raw/ folder)
        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                soundsLoaded = true
                Log.d("MainActivity", "Sounds loaded successfully")
            }
        }

        soundButtonClick = soundPool.load(this, R.raw.button_click, 1)
        soundScoreIncrease = soundPool.load(this, R.raw.score_increase, 1)
        soundDeduction = soundPool.load(this, R.raw.deduction, 1)
        soundComplete = soundPool.load(this, R.raw.complete, 1)

        // Set text color based on current element from ViewModel
        when {
            viewModel.currentElement <= 2 -> scoreTxt.setTextColor(getColor(R.color.basic))
            viewModel.currentElement in 3..6 -> scoreTxt.setTextColor(getColor(R.color.intermediate))
            else -> scoreTxt.setTextColor(getColor(R.color.advanced))
        }
        
        // Restore button states from ViewModel
        if (viewModel.currentElement > 1 && viewModel.currentScore >= 2) {
            deductionBtn.isEnabled = true
        } else {
            deductionBtn.isEnabled = false
        }
        
        Log.d("MainActivity", "onCreate: Current state - Score: ${viewModel.currentScore}, Element: ${viewModel.currentElement}")
        showText()
    }
    private fun showText() {
        scoreTxt.setText(getString(R.string.scoreTxt, viewModel.currentScore))
        if (!isSadMode) {
            updateEmojiIcon()
        }
        // Check if routine is complete (element reached 10)
        if (viewModel.currentElement > 10) {
            elementTxt.setText(getString(R.string.completeRoutineTxt))
            resetBtn.setText(getString(R.string.retryBtn))
            setButtonStates(performEnabled = false, deductionEnabled = false, reason = "Routine complete")
            playSound(soundComplete)
            animateFireworks()
            Log.d("MainActivity", "showText: Routine Complete - Score: ${viewModel.currentScore}")
        } else {
            elementTxt.setText(getString(R.string.elementTxt, viewModel.currentElement))
            resetBtn.setText(getString(R.string.resetBtn))
            Log.d("MainActivity", "showText: Displaying - Score: ${viewModel.currentScore}, Element: ${viewModel.currentElement}")
        }
    }
    
    private fun animateScoreUpdate() {
        scoreTxt.animate()
            .scaleX(1.3f)
            .scaleY(1.3f)
            .setDuration(200)
            .withEndAction {
                scoreTxt.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start()
            }
            .start()
        
        emojiIcon.animate()
            .scaleX(1.3f)
            .scaleY(1.3f)
            .setDuration(200)
            .withEndAction {
                emojiIcon.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start()
            }
            .start()
    }
    
    private fun animateElementUpdate() {
        elementTxt.animate()
            .translationY(-30f)
            .setDuration(300)
            .setInterpolator(BounceInterpolator())
            .withEndAction {
                elementTxt.animate()
                    .translationY(0f)
                    .setDuration(300)
                    .start()
            }
            .start()
    }
    
    private fun animateButton(button: Button) {
        playSound(soundButtonClick)
        button.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100)
            .withEndAction {
                button.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }
    
    private fun animateDeduction() {
        val animator = ObjectAnimator.ofFloat(scoreTxt, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
        animator.duration = 500
        animator.start()
    }
    
    private fun animateFireworks() {
        fireworksView.visibility = View.VISIBLE
        fireworksView.alpha = 0f
        fireworksView.scaleX = 0.5f
        fireworksView.scaleY = 0.5f
        
        // Fade in and scale up
        fireworksView.animate()
            .alpha(1f)
            .scaleX(1.5f)
            .scaleY(1.5f)
            .setDuration(800)
            .setInterpolator(BounceInterpolator())
            .withEndAction {
                // Rotate and fade out
                fireworksView.animate()
                    .rotation(360f)
                    .alpha(0f)
                    .scaleX(2f)
                    .scaleY(2f)
                    .setDuration(1000)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .withEndAction {
                        fireworksView.visibility = View.GONE
                        fireworksView.rotation = 0f
                        fireworksView.scaleX = 1f
                        fireworksView.scaleY = 1f
                    }
                    .start()
            }
            .start()
    }
    
    private fun updateEmojiIcon() {
        when {
            viewModel.currentElement >= 8 -> emojiIcon.setImageResource(R.drawable.fire)
            viewModel.currentElement >= 4 -> emojiIcon.setImageResource(R.drawable.happy)
            else -> emojiIcon.setImageResource(R.drawable.smile)
        }
    }
    
    private fun playSound(soundId: Int, rate: Float = 1.0f) {
        if (soundsLoaded && soundId != 0) {
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, rate)
        }
    }
    
    private fun setButtonStates(performEnabled: Boolean, deductionEnabled: Boolean, reason: String) {
        performBtn.isEnabled = performEnabled
        deductionBtn.isEnabled = deductionEnabled
        Log.d("MainActivity", "setButtonStates: $reason - Perform: ${if(performEnabled) "enabled" else "disabled"}, Deduction: ${if(deductionEnabled) "enabled" else "disabled"}")
    }
    private fun updateScore() {
        val previousElement = viewModel.currentElement
        val previousScore = viewModel.currentScore
        
        isSadMode = false  // Reset sad mode when performing
        
        if (viewModel.currentElement <= 2) {
            viewModel.currentScore++;
            Log.d("MainActivity", "updateScore: Basic level - Score increased by 1")
        }
        else if (viewModel.currentElement in 3..6) {
            viewModel.currentScore += 2;
            scoreTxt.setTextColor(getColor(R.color.intermediate))
            Log.d("MainActivity", "updateScore: Intermediate level - Score increased by 2, Color changed")
        }
        else if (viewModel.currentElement in 7..10) {
            viewModel.currentScore += 3;
            scoreTxt.setTextColor(getColor(R.color.advanced))
            Log.d("MainActivity", "updateScore: Advanced level - Score increased by 3, Color changed")
        }

        if (viewModel.currentScore > 20) {
            viewModel.currentScore = 20
            Log.d("MainActivity", "updateScore: Score capped at maximum 20")
        }

        viewModel.currentElement++;
        Log.d("MainActivity", "updateScore: Element advanced from $previousElement to ${viewModel.currentElement}")
        Log.d("MainActivity", "updateScore: Score changed from $previousScore to ${viewModel.currentScore}")

        animateScoreUpdate()
        animateElementUpdate()
        
        // Play sound with increasing pitch based on element level
        val soundRate = when {
            viewModel.currentElement <= 2 -> 1.0f
            viewModel.currentElement in 3..6 -> 1.2f
            else -> 1.5f
        }
        playSound(soundScoreIncrease, soundRate)
        
        if (viewModel.currentElement > 1 && viewModel.currentScore >= 2 && !deductionBtn.isEnabled) { 
            setButtonStates(performEnabled = performBtn.isEnabled, deductionEnabled = true, reason = "Score threshold met")
        };
    }

    private fun resetScore() {
        Log.d("MainActivity", "resetScore: Resetting all elements to initial state")
        Log.d("MainActivity", "resetScore: Previous state - Score: ${viewModel.currentScore}, Element: ${viewModel.currentElement}")
        isSadMode = false
        scoreTxt.setTextColor(getColor(R.color.basic))
        viewModel.currentScore = 0;
        viewModel.currentElement = 1;
        resetBtn.setText(getString(R.string.resetBtn))
        setButtonStates(performEnabled = true, deductionEnabled = false, reason = "Reset complete")
        Log.d("MainActivity", "resetScore: Reset complete - Score: ${viewModel.currentScore}, Element: ${viewModel.currentElement}")
    }

    private fun onDeducted() {
        Log.d("checkDeductedAnimation", "call function onDeducted")
        if (viewModel.currentElement <= 1) {
            Log.d("MainActivity", "onDeducted: Cannot deduct - Element is at initial state")
            return
        }
        val previousScore = viewModel.currentScore
        viewModel.currentScore -= 2;
        
        // Change to sad icon and animate
        isSadMode = true
        emojiIcon.setImageResource(R.drawable.sad)
        emojiIcon.animate()
            .scaleX(1.5f)
            .scaleY(1.5f)
            .setDuration(200)
            .withEndAction {
                emojiIcon.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start()
            }
            .start()
        
        Log.d("MainActivity", "onDeducted: Deduction applied - Score changed from $previousScore to ${viewModel.currentScore}")
        playSound(soundDeduction)
        animateDeduction()
        setButtonStates(performEnabled = false, deductionEnabled = false, reason = "After deduction penalty")
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.performBtn -> {
                animateButton(performBtn)
                Log.d("MainActivity", "onClick: Perform button clicked")
                if (viewModel.currentElement <= 10) {
                    Log.d("MainActivity", "onClick: Conditions met - Updating score")
                    updateScore();
                } else {
                    Log.d("MainActivity", "onClick: Cannot perform - Element: ${viewModel.currentElement} already complete")
                }
            }
            R.id.resetBtn -> {
                animateButton(resetBtn)
                Log.d("MainActivity", "onClick: Reset button clicked")
                resetScore()
            }
            R.id.deductionBtn -> {
                animateButton(deductionBtn)
                Log.d("MainActivity", "onClick: Deduction button clicked")
                onDeducted();
            }
        }
        showText();
    }
    
    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}