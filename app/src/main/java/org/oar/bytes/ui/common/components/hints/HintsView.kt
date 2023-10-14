package org.oar.bytes.ui.common.components.hints

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import org.json.JSONObject
import org.oar.bytes.R
import org.oar.bytes.utils.JsonExt.getJSONArrayOrNull
import org.oar.bytes.utils.JsonExt.jsonArray

class HintsView(
    context: Context,
    attr: AttributeSet? = null
) : FrameLayout(context, attr) {

    private var onRevertClickListener: OnClickListener? = null
    fun setOnRevertClickListener(listener: OnClickListener) { onRevertClickListener = listener }

    private val hints: List<HintButtonView>

    init {
        LayoutInflater.from(context).inflate(R.layout.component_hints_bar, this, true)

        val revert = findViewById<HintButtonView>(R.id.revertLastBtn)
        revert.maxValue = 100
        revert.setOnClickListener {
            onRevertClickListener?.onClick(it)
            revert.setProgress(0)
        }
        // DEBUG PURPOSES
        revert.setOnLongClickListener {
            onRevertClickListener?.onClick(it)
            true
        }

        hints = listOf(revert)
    }

    fun addProgress(value: Int) {
        hints.forEach { it.addProgress(value) }
    }

    fun appendToJson(json: JSONObject) {
        hints
            .map { it.currentValue }
            .jsonArray()
            .also { json.put("hints", it) }
    }

    fun fromJson(json: JSONObject) {
        json.getJSONArrayOrNull("hints")?.also { hintsArray ->
            val length = hintsArray.length()
            hints.indices.forEach { i ->
                if (i < length) {
                    hints[i].setProgress(hintsArray.getInt(i))
                }
            }
        }
    }
}