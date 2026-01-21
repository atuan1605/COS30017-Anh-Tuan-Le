package com.example.calculatorweek3

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var num1: EditText;
    lateinit var num2: EditText;
    lateinit var sum: CheckBox;
    lateinit var minus: CheckBox;
    lateinit var result: TextView;
    lateinit var btn: Button;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        num1 = findViewById(R.id.num1);
        num2 = findViewById(R.id.num2);
        sum = findViewById(R.id.sum);
        minus = findViewById(R.id.minus);
        result = findViewById(R.id.result);
        btn = findViewById(R.id.button)
        sum.setOnClickListener(this)
        minus.setOnClickListener(this)
        btn.setOnClickListener(this)

    }

    override fun onClick(v: View?) {

        when(v?.id) {
            R.id.sum -> {
                minus.isChecked = false
            }
            R.id.minus -> {
                sum.isChecked = false
            }
            R.id.button -> {
                if (!num1.text.isEmpty() && !num2.text.isEmpty()) {
                   if (sum.isChecked) {
                       val temp = num1.text.toString().toDouble() + num2.text.toString().toDouble()
                       result.setText("$temp")
                   }
                    if (minus.isChecked) {
                        val temp = num1.text.toString().toDouble() - num2.text.toString().toDouble()
                        result.setText("$temp")
                    }
                }
            }
        }
    }
}