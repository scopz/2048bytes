package org.oar.bytes.ui.common.components.levelpanel

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import org.json.JSONObject
import org.oar.bytes.R
import org.oar.bytes.model.AnimatedValue
import org.oar.bytes.model.SByte
import org.oar.bytes.utils.ComponentsExt.runOnUiThread
import org.oar.bytes.utils.Constants
import org.oar.bytes.utils.Data
import org.oar.bytes.utils.NumbersExt.color
import org.oar.bytes.utils.NumbersExt.sByte
import java.util.function.Consumer

class LevelPanelView(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val levelUpButton by lazy { findViewById<TextView>(R.id.levelUpButton) }
    private val capacityUpButton by lazy { findViewById<ImageView>(R.id.capacityUpButton) }

    private val progressBar by lazy { findViewById<ProgressBarView>(R.id.progressBar) }

    var capacity = 256.sByte
        private set(value) {
            field = value
            progressBar.setCapacityProgress(storedValue.value, capacity)
        }

    var toLevel: SByte
        get() = Constants.LEVEL_EXP[Data.gameLevel - 1]
        private set(value) {
            progressBar.setLevelProgress(storedValue.value, value)
        }

    val storedValue = AnimatedValue(0.sByte)
        .apply {
            onValueChanged = { updateUi() }
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
        Data.getBytes = { storedValue.value }
        Data.consumeBytes = { bytes ->
            if (bytes.isNegative) {
                storedValue.operate {
                    (it - bytes).coerceAtMost(capacity)
                }
                true
            } else if (storedValue.value >= bytes) {
                storedValue.value -= bytes
                true
            } else false
        }

        LayoutInflater.from(context).inflate(R.layout.component_level_panel, this, true)

        findViewById<TextView>(R.id.levelUpButton).setOnClickListener {
            if (storedValue.value >= toLevel) {
                val expRequired = toLevel
                Data.gameLevel++
                storedValue.value -= expRequired
                levelUpButton.text = Data.gameLevel.toString()
                onLevelUpListener?.accept(Data.gameLevel)

                if (storedValue.value < toLevel) {
                    newLevelReached = false
                    onNewLevelReachedListener?.accept(false)
                }
            }
        }

        capacityUpButton.setOnClickListener {
            capacity += storedValue.value
            storedValue.value = 0.sByte
            capacityReached = false
            onCapacityReachedListener?.accept(false)
        }

        levelUpButton.text = Data.gameLevel.toString()
    }

    fun addBytes(value: SByte, inAnimation: Boolean = false) {
        if (value.isZero) return
        storedValue.operate(inAnimation) {
            (it + value).coerceAtMost(capacity)
        }
    }

    fun appendToJson(json: JSONObject) {
        json.apply {
            put("storedValue", storedValue.finalValue.value.toString())
            put("capacity", capacity.value.toString())
        }
    }

    private fun updateUi() {
        val levelUpBackgroundEdit =
            if (storedValue.value >= toLevel) {
                if (!newLevelReached) true
                else null
            } else if (newLevelReached) false
            else null

        levelUpBackgroundEdit?.let {
            newLevelReached = it
            onNewLevelReachedListener?.accept(it)
        }

        val capacityBackgroundEdit = if (storedValue.value >= capacity) {
                if (!capacityReached) true
                else null
            } else if (capacityReached) false
            else null

        capacityBackgroundEdit?.let {
            capacityReached = it
            onCapacityReachedListener?.accept(it)
        }

        runOnUiThread {
            progressBar.setCapacityProgress(storedValue.value, capacity)
            progressBar.setLevelProgress(storedValue.value, toLevel)

            levelUpBackgroundEdit
                ?.let { if (it) R.color.levelColor else R.color.itemDefaultBackground }
                ?.color(context)
                ?.also { levelUpButton.setBackgroundColor(it) }

            capacityBackgroundEdit
                ?.let { if (it) R.color.capacityColor else R.color.itemDefaultBackground }
                ?.color(context)
                ?.also { capacityUpButton.setBackgroundColor(it) }
        }
    }

    fun fromJson(json: JSONObject) {
        capacity = json.getString("capacity").sByte
        storedValue.value = json.getString("storedValue").sByte
        levelUpButton.text = Data.gameLevel.toString()
    }
}