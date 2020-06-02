package com.demo.rangeviewdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.vin.rangeview.view.CustomRangeView
import com.vin.rangeview.view.ERROR
import com.vin.rangeview.view.OnRangeValueChangeListener

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val crv = findViewById<CustomRangeView>(R.id.crvDemo)
        val tvRange = findViewById<TextView>(R.id.tvRange)

        crv.onRangeValueChangeListener = object : OnRangeValueChangeListener {
            override fun onValueChanged(oldValue: Int, newValue: Int) {
                tvRange.text = "Range Value is $newValue"
            }

            override fun onError(error: ERROR) {
                Toast.makeText(this@MainActivity, error.name, Toast.LENGTH_SHORT).show()
            }
        }
    }
}