package org.oar.bytes.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.annotation.ColorRes
import org.oar.bytes.utils.extensions.NumbersExt.color

class ColoredRect(
    private val context: Context,
    private val view: View,
    @ColorRes color: Int
) {
    private val rect = Rect()
    private val paint = Paint().apply {
        this.color = color.color(context)
    }

    var bottom: Int set(value) { rect.bottom = value } get() = rect.bottom
    var top: Int set(value) { rect.top = value } get() = rect.top
    var right: Int set(value) { rect.right = value } get() = rect.right
    var left: Int set(value) { rect.left = value } get() = rect.left
    fun width() = rect.width()
    fun height() = rect.height()

    var percentWidth: Float = 0f
        set(value) {
            field = value
            rect.right = (view.measuredWidth * value).toInt()
        }

    var percentBottomHeight: Float = 0f
        set(value) {
            field = value
            rect.top =
                if (value <= 0f) view.measuredHeight
                else (view.measuredHeight * (1f-value)).toInt()
        }

    companion object {
        fun Canvas.drawRect(cRect: ColoredRect) {
            this.drawRect(cRect.rect, cRect.paint)
        }
    }
}