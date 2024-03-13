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

    private var onImproveLowerClickListener: Runnable? = null
    fun setOnImproveLowerClickListener(listener: Runnable) { onImproveLowerClickListener = listener }

    private var onRevertClickListener: Runnable? = null
    fun setOnRevertClickListener(listener: Runnable) { onRevertClickListener = listener }

    private var onSwapClickListener: Consumer<Boolean>? = null
    fun setOnSwapClickListener(listener: Consumer<Boolean>) { onSwapClickListener = listener }

    private var onRemoveClickListener: Consumer<Boolean>? = null
    fun setOnRemoveClickListener(listener: Consumer<Boolean>) { onRemoveClickListener = listener }

    val hints: List<HintButtonView>

    val add: HintButtonView
    val improveLower: HintButtonView
    val revert: HintButtonView
    val swap: HintButtonView
    val remove: HintButtonView

    init {
        LayoutInflater.from(context).inflate(R.layout.component_hints_bar, this, true)

        add = findViewById(R.id.addBtn)
        add.maxValue = 10800 // 3h
        add.setOnClickListener {
            if (add.active) {
                onAddClickListener?.accept(false)
            } else if (canActivate()) {
                onAddClickListener?.accept(true)
            }
        }

        improveLower = findViewById(R.id.improveLower)
        improveLower.maxValue = 21600 // 6h
        improveLower.setOnClickListener {
            onImproveLowerClickListener?.run()
        }

        revert = findViewById(R.id.revertLastBtn)
        revert.maxValue = 32400 // 9h
        revert.setOnClickListener {
            onRevertClickListener?.run()
        }

        swap = findViewById(R.id.swapBtn)
        swap.maxValue = 50400 // 14h
        swap.setOnClickListener {
            if (swap.active) {
                onSwapClickListener?.accept(false)
            } else if (canActivate()) {
                onSwapClickListener?.accept(true)
            }
        }

        remove = findViewById(R.id.removeBtn)
        remove.maxValue = 72000 // 20h
        remove.setOnClickListener {
            if (remove.active) {
                onRemoveClickListener?.accept(false)
            } else if (canActivate()) {
                onRemoveClickListener?.accept(true)
            }
        }

        hints = listOf(add, improveLower, revert, swap, remove)
    }

    private fun canActivate() = hints.none { it.active }

    fun addProgress(value: Int) {
        hints.forEach { it.addProgress(value) }
    }

    fun addProgress(value: Int, index: Int) {
        hints[index].addProgress(value)
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