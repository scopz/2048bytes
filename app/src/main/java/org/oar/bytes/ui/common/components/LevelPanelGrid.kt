package org.oar.bytes.ui.common.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import org.json.JSONObject
import org.oar.bytes.R
import org.oar.bytes.model.SByte
import org.oar.bytes.utils.Constants
import org.oar.bytes.utils.Data
import org.oar.bytes.utils.NumbersExt.color
import org.oar.bytes.utils.NumbersExt.sByte
import java.util.function.Consumer

class LevelPanelGrid(
    context: Context,
    attr: AttributeSet? = null
) : FrameLayout(context, attr) {

    private val levelUpButton by lazy { findViewById<TextView>(R.id.levelUpButton) }
    private val capacityUpButton by lazy { findViewById<TextView>(R.id.capacityUpButton) }

    private val progressBar by lazy { findViewById<ProgressBarView>(R.id.progressBar) }

    private var capacity = 256.sByte
        set(value) {
            field = value
            progressBar.setCapacityProgress(storedValue, capacity)
        }

    private var toLevel: SByte
        get() {
            val index = Data.gridLevel - 1
            val exp = if (index < Constants.LEVEL_EXP.size) Constants.LEVEL_EXP[index]
                else Constants.LEVEL_EXP.last()
            return exp.sByte
        }
        set(value) {
            progressBar.setLevelProgress(storedValue, value)
        }

    var storedValue = 0.sByte
        set(value) {
            field = value
            progressBar.setCapacityProgress(storedValue, capacity)
            progressBar.setLevelProgress(storedValue, toLevel)

            capacityUpButton.setBackgroundColor(
                if (field >= capacity) R.color.capacityColor.color(context)
                else Constants.SHADE_COLORS[0]
            )

            levelUpButton.setBackgroundColor(
                if (field >= toLevel) R.color.levelColor.color(context)
                else Constants.SHADE_COLORS[0]
            )
        }

    // listeners
    private var onLevelUpListener: Consumer<Int>? = null
    fun setLevelUpListener(listener: Consumer<Int>) { onLevelUpListener = listener }

    init {
        LayoutInflater.from(context).inflate(R.layout.component_level_panel, this, true)

        findViewById<TextView>(R.id.levelUpButton).setOnClickListener {
            if (storedValue >= toLevel) {
                storedValue -= toLevel
                Data.gridLevel++
                levelUpButton.text = Data.gridLevel.toString()
                onLevelUpListener?.accept(Data.gridLevel)
            }
        }

        findViewById<TextView>(R.id.capacityUpButton).setOnClickListener {
            capacity += storedValue
            storedValue = 0.sByte
        }

        storedValue = 0.sByte
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