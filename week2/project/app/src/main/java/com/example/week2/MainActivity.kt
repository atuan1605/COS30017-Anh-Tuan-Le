package com.example.week2

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var txtNum1: EditText
    lateinit var txtNum2: EditText
    lateinit var txtNum3: TextView
    lateinit var btnSum: Button
    lateinit var btnMinus: Button
    lateinit var btnTimes: Button
    lateinit var btnDevide: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        txtNum1 = findViewById(R.id.edtNum1)
        txtNum2= findViewById(R.id.edtNum2)
        txtNum3= findViewById(R.id.textView3)
        btnSum= findViewById(R.id.button)
        btnMinus= findViewById(R.id.button2)
        btnTimes= findViewById(R.id.button3)
        btnDevide= findViewById(R.id.button4)

        btnSum.setOnClickListener (this)
        btnMinus.setOnClickListener (this)
        btnTimes.setOnClickListener (this)
        btnDevide.setOnClickListener (this)
    }

    override fun onClick(v: View?) {
        val str1 = txtNum1.text.toString()
        val str2= txtNum2.text.toString()
        if (str1.isEmpty() || str2.isEmpty()) {
            txtNum3.setText("Please enter both numbers")
            return
        }
        val temp1 = str1.toDouble()
        val temp2 = str2.toDouble()
        val result = when(v?.id) {
            R.id.button -> temp1 + temp2
            R.id.button2 -> temp1 - temp2
            R.id.button3 -> temp1 * temp2
            R.id.button4 -> if (temp2 != 0.0 ) temp1 / temp2 else return
            else -> return
        }
        txtNum3.setText(getString(R.string.result_text, result))
    }
}