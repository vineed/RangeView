package com.vin.rangeview.view

import android.content.Context
import android.os.Handler
import android.text.InputFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import com.vin.rangeview.R
import com.vin.rangeview.rangeview.listener.SimpleTouchListener
import kotlinx.android.synthetic.main.range_layout.view.*


const val COUNTER_STEP_TIME = 100L
const val COUNTER_START_STEP_TIME = 500L

class CustomRangeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    FrameLayout(context, attrs, defStyleAttr) {
    var layoutInflater: LayoutInflater = LayoutInflater.from(context)

    var minRange: Int = 0
        set(value) {
            setRange(range, value)
            field = value
        }

    var maxRange: Int = Integer.MAX_VALUE
        set(value) {
            setRange(range, minRange)
            field = value
        }

    var range: Int = minRange
        set(value) {
            field = if (value < minRange || value > maxRange) {
                minRange
            } else {
                value
            }

            if (value != field)
                setRange(value, field)
        }

    private var shouldStopCount = false
    lateinit var etValue: EditText

    //private val onRangeChangeListener: OnRangeChangeListener? = null
    var onRangeValueChangeListener: OnRangeValueChangeListener? = null

    init {
        val view =
            LayoutInflater.from(context).inflate(R.layout.range_layout, this, true)

        etValue = findViewById(R.id.etValue)
        etValue.setText(range.toString())

        val filter = InputFilter { source, start, end, dest, dstart, dend ->
            val ogValue = dest.toString().replaceRange(dstart, dend, source)

            try {
                val numValue = ogValue.toInt()
                if (numValue > maxRange) {
                    onRangeValueChangeListener?.onError(ERROR.OUT_OF_MAX_RANGE)
                    return@InputFilter ""
                }
            } catch (nfe: NumberFormatException) {
                nfe.printStackTrace()
            }

            return@InputFilter null
        }

        etValue.filters = arrayOf(filter)

        tvMinus.setOnClickListener {
            processRange(false)
        }

        tvPlus.setOnClickListener {
            processRange(true)
        }

        val handler = Handler()

        val onLongMinus = object : Runnable {
            override fun run() {
                if (!shouldStopCount) {
                    processRange(false)
                    handler.postDelayed(
                        this,
                        COUNTER_STEP_TIME
                    )
                }
            }
        }

        val onLongPlus = object : Runnable {
            override fun run() {
                if (!shouldStopCount) {
                    processRange(true)
                    handler.postDelayed(
                        this,
                        COUNTER_STEP_TIME
                    )
                }
            }
        }

        tvMinus.setOnTouchListener(object : SimpleTouchListener() {
            override fun onDownTouchAction(): Boolean {
                shouldStopCount = false
                handler.postDelayed(
                    onLongMinus,
                    COUNTER_START_STEP_TIME
                )

                return false
            }

            override fun onUpTouchAction(): Boolean {
                shouldStopCount = true
                handler.removeCallbacks(onLongMinus)

                return false
            }

            override fun onCancelTouchAction(): Boolean {
                shouldStopCount = true
                handler.removeCallbacks(onLongMinus)

                return false
            }
        })

        tvPlus.setOnTouchListener(object : SimpleTouchListener() {
            override fun onDownTouchAction(): Boolean {
                shouldStopCount = false
                handler.postDelayed(
                    onLongPlus,
                    COUNTER_START_STEP_TIME
                )

                return false
            }

            override fun onUpTouchAction(): Boolean {
                shouldStopCount = true
                handler.removeCallbacks(onLongPlus)

                return false
            }

            override fun onCancelTouchAction(): Boolean {
                shouldStopCount = true
                handler.removeCallbacks(onLongPlus)

                return false
            }
        })

        etValue.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                processRangeInput()

                etValue.clearFocus()
            }

            false
        }

        etValue.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                etValue.selectAll()
            } else processRangeInput()
        }

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CustomRangeView,
            0, 0
        ).apply {

            try {
                minRange = getInt(R.styleable.CustomRangeView_minRange, 0)
                maxRange = getInt(R.styleable.CustomRangeView_maxRange, Integer.MAX_VALUE)
                range = getInt(R.styleable.CustomRangeView_range, Integer.MAX_VALUE)
            } finally {
                recycle()
            }
        }
    }

    private fun setRange(oldValue: Int, value: Int) {
        range = value
        etValue.setText(range.toString())
        onRangeValueChangeListener?.onValueChanged(oldValue, range)
    }

    private fun processRange(inc: Boolean) {
        val isMinReached = range == minRange
        val isMaxReached = range == maxRange
        if ((isMinReached && !inc) || (isMaxReached && inc)) {
            shouldStopCount = true
            onRangeValueChangeListener?.onError(if (isMinReached) ERROR.MIN_VALUE_REACHED else ERROR.MAX_VALUE_REACHED)
        } else {
            val oldValue = range
            range = if (inc) range.inc() else range.dec()

            etValue.setText(range.toString())

            onRangeValueChangeListener?.onValueChanged(oldValue, range)
        }
    }

    private fun processRangeInput() {
        try {
            val inputValue = etValue.text.toString().toInt()

            val isAboveMax = inputValue > maxRange
            val isBelowMin = inputValue < minRange
            if (isAboveMax || isBelowMin) {
                etValue.setText(range.toString())
                onRangeValueChangeListener?.onError(if (isAboveMax) ERROR.OUT_OF_MIN_RANGE else ERROR.OUT_OF_MAX_RANGE)
            } else {
                setRange(range, inputValue)
            }
        } catch (nfe: NumberFormatException) {
            etValue.setText(range.toString())
            onRangeValueChangeListener?.onError(ERROR.INVALID_NUMBER)
            nfe.printStackTrace()
        }
    }
}

/*interface OnRangeChangeListener {
    fun onRangeIncreased(value: Int)
    fun onRangeDecreased(value: Int)
}*/

enum class ERROR {
    OUT_OF_MIN_RANGE,
    OUT_OF_MAX_RANGE,
    MAX_VALUE_REACHED,
    MIN_VALUE_REACHED,
    INVALID_NUMBER
}

interface OnRangeValueChangeListener {
    fun onValueChanged(oldValue: Int, newValue: Int)

    fun onError(error: ERROR)
}
