package org.oar.bytes.ui.components.grid

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import org.oar.bytes.R
import org.oar.bytes.model.Position
import org.oar.bytes.model.SByte
import org.oar.bytes.utils.NumbersExt.color
import kotlin.math.abs

open class GridTile(
    private val context: Context,
    val value: SByte,
    val pos: Position,
    var level: Int
): Cloneable {

    private val levelColor
        get() = when(level) {
            1 -> R.color.shade01.color(context)
            2 -> R.color.shade02.color(context)
            3 -> R.color.shade03.color(context)
            4 -> R.color.shade04.color(context)
            5 -> R.color.shade05.color(context)
            6 -> R.color.shade06.color(context)
            7 -> R.color.shade07.color(context)
            8 -> R.color.shade08.color(context)
            9 -> R.color.shade09.color(context)
            10 -> R.color.shade10.color(context)
            else -> R.color.shade11.color(context)
        }

    private val bumpColors = listOf(
        R.color.bump01.color(context),
        R.color.bump02.color(context),
        R.color.bump03.color(context),
        R.color.bump04.color(context),
        R.color.bump05.color(context),
        R.color.bump06.color(context),
        R.color.bump07.color(context),
        Color.BLACK,
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
            pointX = if (dx > 0) x + tileSpeed else x - tileSpeed
            return true
        }

        if (x == destinyPointX) {
            val dy = destinyPointY - y

            if (abs(dy) < tileSpeed) {
                pointY = destinyPointY
                return false
            }
            pointY = if (dy > 0) y + tileSpeed else y - tileSpeed
            return true
        }

        return false
    }

    fun advanceLevel() {
        level++
    }

    fun draw(canvas: Canvas, size: Int) {
        val left = pointX ?: (pos.x * size)
        val top = pointY ?: (pos.y * size)

        val rect = Rect(left, top, left + size, top + size)

        val paint = Paint()
        paint.color = if (activeColor == Color.BLACK) levelColor else activeColor

        val textPaint = Paint()
        textPaint.color = Color.WHITE
        textPaint.textSize = 60f
        textPaint.textAlign = Paint.Align.CENTER

        canvas.drawRect(rect, paint)

        canvas.drawText(value.toString(), left.toFloat() + size/2, top.toFloat() + size/2, textPaint)
    }

    override fun toString(): String {
        return "GridTile(value=$value, pos=[${pos.x}, ${pos.y}, level=$level])"
    }

    public override fun clone() = GridTile(context, value.clone(), pos, level)
}