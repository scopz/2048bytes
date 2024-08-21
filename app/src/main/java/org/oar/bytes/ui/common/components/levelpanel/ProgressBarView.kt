package org.oar.bytes.ui.common.components.levelpanel

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import org.oar.bytes.R
import org.oar.bytes.model.SByte
import org.oar.bytes.ui.common.LimitedDrawFrameLayout
import org.oar.bytes.utils.NumbersExt.color

class ProgressBarView(
    context: Context,
    attrs: AttributeSet? = null
) : LimitedDrawFrameLayout(context, attrs) {

    private var capacityProgress = 0f
        set(value) {
            field = value
            capacityRect.right = (measuredWidth * value).toInt()
            postInvalidate()
        }

    private var levelProgress = 0f
        set(value) {
            field = value
            levelRect.right = (measuredWidth * value).toInt()
            postInvalidate()
        }

    private val capacityRect = Rect()
    private val levelRect = Rect()
    private val capacityBar = Paint()
    private val levelBar = Paint()

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

        capacityBar.color = R.color.capacityColor.color(context)
        levelBar.color = R.color.levelColor.color(context)

        if (capacityProgress > 0) capacityRect.right = (measuredWidth * capacityProgress).toInt()
        if (levelProgress > 0) levelRect.right = (measuredWidth * levelProgress).toInt()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(capacityRect, capacityBar)
        canvas.drawRect(levelRect, levelBar)
    }
}