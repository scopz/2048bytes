package org.oar.bytes.ui.components.grid

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import org.oar.bytes.model.Position
import org.oar.bytes.model.SByte
import kotlin.math.abs

open class GridTile(
    val value: SByte,
    val pos: Position,
): Cloneable {
    private val bumpColors = listOf(
        Color.WHITE,
        Color.YELLOW,
        Color.GREEN,
        Color.CYAN,
        Color.BLUE,
        Color.MAGENTA,
        Color.RED,
        Color.BLACK
    )

    private var activeColor = bumpColors.last()

    private var pointX: Int? = null
    private var pointY: Int? = null

    private var destinyPointX = 0
    private var destinyPointY = 0

    private var tileSpeed = 1

    fun prepareBumpAnimation() {
        activeColor = bumpColors.first()
    }

    fun bumpAnimation(): Boolean {
        if (activeColor != bumpColors.last()) {
            activeColor = bumpColors[bumpColors.indexOf(activeColor)+1]
        }
        if (activeColor == bumpColors.last()) {
            return false
        }
        return true
    }

    fun prepareStepAnimation(size: Int, goTo: Position, tileSpeed: Int) {
        pointX = pos.x * size
        pointY = pos.y * size

        destinyPointX = goTo.x * size
        destinyPointY = goTo.y * size

        this.tileSpeed = tileSpeed
    }

    fun stepAnimation(): Boolean {
        val x = pointX ?: return false
        val y = pointY ?: return false

        if (y == destinyPointY) {
            val dx = destinyPointX - x

            if (abs(dx) < tileSpeed) {
                pointX = destinyPointX
                return false
            }

            pointX = if (dx > 0) {
                x + tileSpeed
            } else {
                x - tileSpeed
            }
            return true
        }

        if (x == destinyPointX) {
            val dy = destinyPointY - y

            if (abs(dy) < tileSpeed) {
                pointY = destinyPointY
                return false
            }

            pointY = if (dy > 0) {
                y + tileSpeed
            } else {
                y - tileSpeed
            }
            return true
        }

        return false
    }

    fun draw(canvas: Canvas, size: Int) {
        val left = pointX ?: (pos.x * size)
        val top = pointY ?: (pos.y * size)

        val rect = Rect(left, top, left + size, top + size)

        val paint = Paint()
        paint.color = activeColor

        val textPaint = Paint()
        textPaint.color = Color.WHITE
        textPaint.textSize = 60f
        textPaint.textAlign = Paint.Align.CENTER

        canvas.drawRect(rect, paint)

        canvas.drawText(value.toString(), left.toFloat() + size/2, top.toFloat() + size/2, textPaint)
    }

    override fun toString(): String {
        return "GridTile(value=$value, pos=[${pos.x}, ${pos.y}])"
    }

    public override fun clone() = GridTile(value.clone(), pos)
}