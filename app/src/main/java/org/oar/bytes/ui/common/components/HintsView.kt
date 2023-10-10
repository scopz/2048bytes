package org.oar.bytes.ui.common.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import org.oar.bytes.R

class HintsView(
    context: Context,
    attr: AttributeSet? = null
) : FrameLayout(context, attr) {

    private var onRevertClickListener: OnClickListener? = null
    fun setOnRevertClickListener(listener: OnClickListener) { onRevertClickListener = listener }

    init {
        LayoutInflater.from(context).inflate(R.layout.component_hints_bar, this, true)

        findViewById<TextView>(R.id.revertLastBtn).setOnClickListener {
            onRevertClickListener?.onClick(it)
        }
    }
}