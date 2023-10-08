package org.oar.bytes.ui.common.components.levelpanel

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.TextView
import org.json.JSONObject
import org.oar.bytes.R
import org.oar.bytes.model.SByte
import org.oar.bytes.utils.Constants
import org.oar.bytes.utils.Data
import org.oar.bytes.utils.NumbersExt.sByte
import java.util.function.Consumer

class LevelPanelGrid(
    context: Context,
    attr: AttributeSet? = null
) : FrameLayout(context, attr) {

    private val stored by lazy { findViewById<TextView>(R.id.stored) }
    private val seekBar by lazy { findViewById<SeekBar>(R.id.seekBar) }
    private val levelUpButton by lazy { findViewById<TextView>(R.id.levelUpButton) }
    private val capacityUpButton by lazy { findViewById<TextView>(R.id.capacityUpButton) }

    private var capacity = 300.sByte
        set(value) {
            field = value
            seekBar.max = value.value.toInt()
        }

    var storedValue = 0.sByte
        set(value) {
            field = value
            stored.text = storedValue.toString()
            seekBar.progress = value.value.toInt()

            if (field == capacity) {
                capacityUpButton.setBackgroundColor(Color.CYAN)
            } else {
                capacityUpButton.setBackgroundColor(Constants.SHADE_COLORS[0])
            }
        }

    // listeners
    private var onLevelUpListener: Consumer<Int>? = null
    fun setLevelUpListener(listener: Consumer<Int>) { onLevelUpListener = listener }

    init {
        LayoutInflater.from(context).inflate(R.layout.component_level_panel, this, true)

        findViewById<TextView>(R.id.levelUpButton).setOnClickListener {
            Data.gridLevel++
            onLevelUpListener?.accept(Data.gridLevel)
            levelUpButton.text = Data.gridLevel.toString()
        }

        findViewById<TextView>(R.id.capacityUpButton).setOnClickListener {
            capacity += storedValue
            storedValue = 0.sByte
        }

        stored.text = storedValue.toString()
        seekBar.isEnabled = false
        seekBar.max = capacity.value.toInt()
        levelUpButton.text = Data.gridLevel.toString()
    }

    fun addBytes(value: SByte) {
        val sum = storedValue + value
        storedValue = if (sum > capacity) capacity else sum
    }

    fun appendToJson(json: JSONObject) {
        json.apply {
            put("storedValue", storedValue.value.toString())
            put("capacity", capacity.value.toString())
        }
    }

    fun fromJson(json: JSONObject) {
        capacity = json.getString("capacity").sByte
        storedValue = json.getString("storedValue").sByte

        levelUpButton.text = Data.gridLevel.toString()
    }
}