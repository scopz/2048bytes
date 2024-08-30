package org.oar.bytes.ui.common.components.levelpanel

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import org.json.JSONObject
import org.oar.bytes.R
import org.oar.bytes.model.SByte
import org.oar.bytes.utils.Constants
import org.oar.bytes.utils.Data
import org.oar.bytes.utils.extensions.ComponentsExt.runOnUiThread
import org.oar.bytes.utils.extensions.NumbersExt.color

class LevelPanelView(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val levelUpButton by lazy { findViewById<TextView>(R.id.levelUpButton) }
    private val capacityUpButton by lazy { findViewById<ImageView>(R.id.capacityUpButton) }

    private val progressBar by lazy { findViewById<ProgressBarView>(R.id.progressBar) }

    var expRequired: SByte
        get() = Constants.LEVEL_EXP[Data.gameLevel.value - 1]
        private set(value) {
            progressBar.setLevelProgress(Data.bytes.value, value)
        }

    // listeners
    private var newLevelReached = false
    var onNewLevelReachedListener: ((Boolean) -> Unit)? = null
    var onNewLevelButtonClickListener: (() -> Unit)? = null

    private var capacityReached = false
    var onCapacityReachedListener: ((Boolean) -> Unit)? = null
    var onCapacityButtonClickListener: (() -> Unit)? = null

    init {
        Data.bytes.observe(context as LifecycleOwner) {
            updateUi()
        }
        Data.capacity.observe(context) {
            if (capacityReached) {
                capacityReached = false
                onCapacityReachedListener?.let { it(false) }
            }
            runOnUiThread {
                progressBar.setCapacityProgress(Data.bytes.value, it)
            }
        }
        Data.gameLevel.observe(context) {
            if (newLevelReached && Data.bytes.value < expRequired) {
                newLevelReached = false
                onNewLevelReachedListener?.let { it(false) }
            }
            runOnUiThread {
                levelUpButton.text = it.toString()
            }
        }

        LayoutInflater.from(context).inflate(R.layout.component_level_panel, this, true)

        findViewById<TextView>(R.id.levelUpButton).setOnClickListener {
            onNewLevelButtonClickListener?.let { it() }
        }

        capacityUpButton.setOnClickListener {
            onCapacityButtonClickListener?.let { it() }
        }

        levelUpButton.text = Data.gameLevel.value.toString()
    }

    private fun updateUi() {
        val levelUpBackgroundEdit =
            if (Data.bytes.value >= expRequired) {
                if (!newLevelReached) true
                else null
            } else if (newLevelReached) false
            else null

        levelUpBackgroundEdit?.let { value ->
            newLevelReached = value
            onNewLevelReachedListener?.let { it(value) }
        }

        val capacityBackgroundEdit = if (Data.bytes.value >= Data.capacity.value) {
                if (!capacityReached) true
                else null
            } else if (capacityReached) false
            else null

        capacityBackgroundEdit?.let { value ->
            capacityReached = value
            onCapacityReachedListener?.let { it(value) }
        }

        runOnUiThread {
            progressBar.setCapacityProgress(Data.bytes.value, Data.capacity.value)
            progressBar.setLevelProgress(Data.bytes.value, expRequired)

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

    fun appendToJson(json: JSONObject) { }

    fun fromJson(json: JSONObject) {
        runOnUiThread {
            levelUpButton.text = Data.gameLevel.value.toString()
        }
    }
}