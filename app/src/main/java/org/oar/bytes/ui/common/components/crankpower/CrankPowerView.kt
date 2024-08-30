package org.oar.bytes.ui.common.components.crankpower

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import org.oar.bytes.R
import org.oar.bytes.ui.common.LimitedDrawFrameLayout
import org.oar.bytes.utils.ColoredRect
import org.oar.bytes.utils.ColoredRect.Companion.drawRect
import org.oar.bytes.utils.extensions.ComponentsExt.runOnUiThread
import org.oar.bytes.utils.extensions.NumbersExt.color

class CrankPowerView(
    context: Context,
    attrs: AttributeSet? = null
) : LimitedDrawFrameLayout(context, attrs) {

    private var numbText = "0 B/s"
    private var numbPercent = 0f
    var numb = true
        set(value) {
            val redraw = field && !value
            field = value
            if (redraw) setData(numbText, numbPercent)
        }

    private var label = 0f
        set(value) {
            field = value
            progressRect.percentWidth = value
            postInvalidate()
        }

    private val progressRect = ColoredRect(context, this, R.color.crankColor)

    private val textView by lazy { findViewById<TextView>(R.id.mainText) }

    init {
        LayoutInflater.from(context).inflate(R.layout.component_progress_bar, this, true)
        findViewById<TextView>(R.id.secondaryText).visibility = GONE

        setBackgroundColor(R.color.itemDefaultBackground.color(context))
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        progressRect.bottom = bottom
        if (label > 0) progressRect.percentWidth = label
    }

    fun setData(text: String, percent: Float) {
        numbText = text
        numbPercent = percent
        if (!numb) {
            runOnUiThread {
                this.textView.text = text
            }
            label = percent
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(progressRect)
    }
}