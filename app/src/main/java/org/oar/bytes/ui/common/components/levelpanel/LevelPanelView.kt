package org.oar.bytes.ui.common.components.levelpanel

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import org.json.JSONObject
import org.oar.bytes.R
import org.oar.bytes.model.SByte
import org.oar.bytes.utils.Constants
import org.oar.bytes.utils.Data
import org.oar.bytes.utils.NumbersExt.color
import org.oar.bytes.utils.NumbersExt.sByte
import java.util.function.Consumer

class LevelPanelView(
    context: Context,
    attr: AttributeSet? = null
) : FrameLayout(context, attr) {

    private val levelUpButton by lazy { findViewById<TextView>(R.id.levelUpButton) }
    private val capacityUpButton by lazy { findViewById<ImageView>(R.id.capacityUpButton) }

    private val progressBar by lazy { findViewById<ProgressBarView>(R.id.progressBar) }

    private var capacity = 256.sByte
        set(value) {
            field = value
            progressBar.setCapacityProgress(storedValue, capacity)
        }

    private var toLevel: SByte
        get() = Constants.LEVEL_EXP[Data.gridLevel - 1]
        set(value) {
            progressBar.setLevelProgress(storedValue, value)
        }

    var storedValue = 0.sByte
        set(value) {
            field = value

            progressBar.setCapacityProgress(storedValue, capacity)
            progressBar.setLevelProgress(storedValue, toLevel)

            if (value >= toLevel) {
                if (!newLevelReached) {
                    levelUpButton.setBackgroundColor(R.color.levelColor.color(context))
                    newLevelReached = true
                    onNewLevelReachedListener?.accept(true)
                }
            } else if (newLevelReached) {
                levelUpButton.setBackgroundColor(R.color.itemDefaultBackground.color(context))
                newLevelReached = false
                onNewLevelReachedListener?.accept(false)
            }

            if (value >= capacity) {
                if (!capacityReached) {
                    capacityUpButton.setBackgroundColor(R.color.capacityColor.color(context))
                    capacityReached = true
                    onCapacityReachedListener?.accept(true)
                }
            } else if (capacityReached) {
                capacityUpButton.setBackgroundColor(R.color.itemDefaultBackground.color(context))
                capacityReached = false
                onCapacityReachedListener?.accept(false)
            }
        }

    // listeners
    private var onLevelUpListener: Consumer<Int>? = null
    fun setLevelUpListener(listener: Consumer<Int>) { onLevelUpListener = listener }

    private var newLevelReached = false
    private var onNewLevelReachedListener: Consumer<Boolean>? = null
    fun setOnNewLevelReachedListener(listener: Consumer<Boolean>) { onNewLevelReachedListener = listener }

    private var capacityReached = false
    private var onCapacityReachedListener: Consumer<Boolean>? = null
    fun setOnCapacityReachedListener(listener: Consumer<Boolean>) { onCapacityReachedListener = listener }

    init {
        Data.getBytes = { storedValue }
        Data.consumeBytes = { bytes ->
            if (storedValue >= bytes) {
                storedValue -= bytes
                true
            } else false
        }

        LayoutInflater.from(context).inflate(R.layout.component_level_panel, this, true)

        findViewById<TextView>(R.id.levelUpButton).setOnClickListener {
            if (storedValue >= toLevel) {
                val expRequired = toLevel
                Data.gridLevel++
                storedValue -= expRequired
                levelUpButton.text = Data.gridLevel.toString()
                onLevelUpListener?.accept(Data.gridLevel)

                if (storedValue < toLevel) {
                    newLevelReached = false
                    onNewLevelReachedListener?.accept(false)
                }
            }
        }

        capacityUpButton.setOnClickListener {
            capacity += storedValue
            storedValue = 0.sByte
            capacityReached = false
            onCapacityReachedListener?.accept(false)
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