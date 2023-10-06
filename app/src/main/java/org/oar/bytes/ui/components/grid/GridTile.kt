package org.oar.bytes.ui.components.grid

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import org.oar.bytes.model.Position
import org.oar.bytes.model.SByte

data class GridTile(
    val value: SByte,
    val pos: Position
) {
    fun draw(canvas: Canvas, size: Int) {
        val left = pos.x * size
        val top = pos.y * size

        val rect = Rect(left, top, left + size, top + size)

        val paint = Paint()
        paint.color = Color.BLUE

        val textPaint = Paint()
        textPaint.color = Color.BLACK
        textPaint.textSize = 30f

        canvas.drawRect(rect, paint)

        canvas.drawText(value.toString(), left.toFloat(), top.toFloat() + size, textPaint)
    }
}