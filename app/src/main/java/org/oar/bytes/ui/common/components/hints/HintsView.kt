package org.oar.bytes.ui.common.components.hints

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import org.json.JSONObject
import org.oar.bytes.R
import org.oar.bytes.utils.JsonExt.getJSONArrayOrNull
import org.oar.bytes.utils.JsonExt.jsonArray
import java.util.function.Consumer

class HintsView(
    context: Context,
    attr: AttributeSet? = null
) : FrameLayout(context, attr) {

    private var onAddClickListener: Consumer<Boolean>? = null
    fun setOnAddClickListener(listener: Consumer<Boolean>) { onAddClickListener = listener }

    private var onRevertClickListener: Runnable? = null
    fun setOnRevertClickListener(listener: Runnable) { onRevertClickListener = listener }

    private var onRemoveClickListener: Consumer<Boolean>? = null
    fun setOnRemoveClickListener(listener: Consumer<Boolean>) { onRemoveClickListener = listener }

    private val hints: List<HintButtonView>

    val add: HintButtonView
    val revert: HintButtonView
    val remove: HintButtonView

    init {
        LayoutInflater.from(context).inflate(R.layout.component_hints_bar, this, true)

        add = findViewById(R.id.addBtn)
        add.maxValue = 3600
        add.setOnClickListener {
            if (add.active) {
                onAddClickListener?.accept(false)
            } else if (canActivate()) {
                onAddClickListener?.accept(true)
            }
        }
        // DEBUG PURPOSES
        add.setOnLongClickListener {
            if (canActivate()) {
                onAddClickListener?.accept(true)
            }
            true
        }

        revert = findViewById(R.id.revertLastBtn)
        revert.maxValue = 7500
        revert.setOnClickListener {
            onRevertClickListener?.run()
            revert.setProgress(0)
        }
        // DEBUG PURPOSES
        revert.setOnLongClickListener {
            onRevertClickListener?.run()
            true
        }


        remove = findViewById(R.id.removeBtn)
        remove.maxValue = 14000
        remove.setOnClickListener {
            if (remove.active) {
                onRemoveClickListener?.accept(false)
            } else if (canActivate()) {
                onRemoveClickListener?.accept(true)
            }
        }
        // DEBUG PURPOSES
        remove.setOnLongClickListener {
            if (canActivate()) {
                onRemoveClickListener?.accept(true)
            }
            true
        }

        hints = listOf(revert, add, remove)
    }

    private fun canActivate() = hints.none { it.active }

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