package org.oar.bytes.ui.common.components.levelpanel

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import org.oar.bytes.R
import org.oar.bytes.model.SByte
import org.oar.bytes.ui.common.LimitedDrawFrameLayout
import org.oar.bytes.utils.ColoredRect
import org.oar.bytes.utils.ColoredRect.Companion.drawRect
import org.oar.bytes.utils.NumbersExt.color

class ProgressBarView(
    context: Context,
    attrs: AttributeSet? = null
) : LimitedDrawFrameLayout(context, attrs) {

    private var capacityProgress = 0f
        set(value) {
            field = value
            capacityRect.percentWidth = value
            postInvalidate()
        }

    private var levelProgress = 0f
        set(value) {
            field = value
            levelRect.percentWidth = value
            postInvalidate()
        }

    private val capacityRect = ColoredRect(context, this, R.color.capacityColor)
    private val levelRect = ColoredRect(context, this, R.color.levelColor)

    private val storedTextView by lazy { findViewById<TextView>(R.id.mainText) }
    private val maxCapacityTextView by lazy { findViewById<TextView>(R.id.secondaryText) }

    init {
        LayoutInflater.from(context).inflate(R.layout.component_progress_bar, this, true)
        setBackgroundColor(R.color.itemDefaultBackground.color(context))
    }

    fun setLevelProgress(current: SByte, max: SByte) {
        levelProgress = (current.value.toFloat() / max.value.toFloat())
    }

    fun setCapacityProgress(current: SByte, max: SByte) {
        capacityProgress = (current.value.toFloat() / max.value.toFloat())
        storedTextView.text = current.toString()
        maxCapacityTextView.text = max.toString()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        levelRect.top = (bottom * 0.9f).toInt()
        capacityRect.bottom = bottom
        levelRect.bottom = bottom

        if (capacityProgress > 0) capacityRect.percentWidth = capacityProgress
        if (levelProgress > 0) levelRect.percentWidth = levelProgress
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(capacityRect)
        canvas.drawRect(levelRect)
    }
}