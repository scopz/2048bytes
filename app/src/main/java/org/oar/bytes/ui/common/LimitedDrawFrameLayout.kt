package org.oar.bytes.ui.common

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout

open class LimitedDrawFrameLayout(
    context: Context,
    attr: AttributeSet? = null
) : FrameLayout(context, attr) {

    private var invalidated = false
    private var nextInvalidationPending = false

    final override fun postInvalidate() {
        if (invalidated) {
            nextInvalidationPending = true
        } else {
            invalidated = true
            super.postInvalidate()
        }
    }

    final override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (nextInvalidationPending) {
            nextInvalidationPending = false
            super.postInvalidate()
        } else {
            invalidated = false
        }
    }
}
