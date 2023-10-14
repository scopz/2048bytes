package org.oar.bytes.ui.common.components.hints

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import org.oar.bytes.R
import org.oar.bytes.utils.NumbersExt.color

class HintButtonView(
    context: Context,
    attr: AttributeSet? = null
) : AppCompatImageView(context, attr) {

    private var setOnReadyListener: Runnable? = null
    fun setOnReadyListener(listener: Runnable) { setOnReadyListener = listener }

    private var onClickListener: OnClickListener? = null
    override fun setOnClickListener(listener: OnClickListener?) { onClickListener = listener }

    var ready = false
        private set

    private var progress = 0f
        set(value) {
            if (value >= 1) {
                field = 1f
                alpha = 1f
                progressRect.top = measuredWidth

                if (!ready) {
                    ready = true
                    setOnReadyListener?.run()
                }
            } else {
                field = value
                ready = false
                alpha = .5f
                progressRect.top = (measuredWidth * (1f-value)).toInt()
            }

            postInvalidate()
        }
    var maxValue = 1
    var currentValue = 0
        private set

    private val progressRect = Rect()
    private val progressPaint = Paint()

    init {
        super.setOnClickListener {
            if (ready) {
                onClickListener?.onClick(it)
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        alpha = if (enabled && progress >= 1) 1f else 0.5f
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        alpha = .5f
        progressRect.top = measuredHeight
        progressRect.bottom = measuredHeight
        progressRect.right = measuredWidth

        progressPaint.color = R.color.hintsColor.color(context)
    }

    fun setProgress(current: Int, max: Int = maxValue) {
        currentValue = current.coerceAtMost(max)
        progress = currentValue.toFloat() / max.toFloat()
    }

    fun addProgress(current: Int, max: Int = maxValue) =
        setProgress(current + currentValue, max)

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(progressRect, progressPaint)
        super.onDraw(canvas)
    }
}