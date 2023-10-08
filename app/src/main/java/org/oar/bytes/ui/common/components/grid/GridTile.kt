package org.oar.bytes.ui.common.components.grid

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.widget.TextView
import org.json.JSONObject
import org.oar.bytes.R
import org.oar.bytes.model.Position
import org.oar.bytes.model.SByte
import org.oar.bytes.utils.Constants.BUMP_COLORS
import org.oar.bytes.utils.Constants.SHADE_COLORS
import kotlin.math.abs

open class GridTile(
    private val context: Context,
    val value: SByte,
    val pos: Position,
    var level: Int,
    val size: Int
): Cloneable {

    private val levelColor
        get() = when {
            level < SHADE_COLORS.size -> SHADE_COLORS[level]
            else -> SHADE_COLORS.last()
        }

    private var activeColor = BUMP_COLORS.last()

    private var pointX: Int? = null
    private var pointY: Int? = null

    private var destinyPointX = 0
    private var destinyPointY = 0

    private var tileSpeed = 1

    private val view: View = View.inflate(context, R.layout.grid_tile, null)
    private val valueView = view.findViewById<TextView>(R.id.value)

    init {
        val measureWidth = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)
        val measuredHeight = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)
        view.measure(measureWidth, measuredHeight)
        view.layout(0, 0, size, size)
        valueView.text = value.toString()
    }

    fun prepareBumpAnimation() {
        activeColor = BUMP_COLORS.first()
    }

    fun bumpAnimation(): Boolean {
        if (activeColor != BUMP_COLORS.last()) {
            activeColor = BUMP_COLORS[BUMP_COLORS.indexOf(activeColor)+1]
        }
        if (activeColor == BUMP_COLORS.last()) {
            return false
        }
        return true
    }

    fun prepareStepAnimation(goTo: Position, tileSpeed: Int) {
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

    fun advanceTileLevel() {
        value.doubleValue()
        valueView.text = value.toString()
        level++
    }

    fun advancedGridLevel() {
        value.doubleValue()
        valueView.text = value.toString()
    }

    fun draw(canvas: Canvas) {
        val left = pointX ?: (pos.x * size)
        val top = pointY ?: (pos.y * size)

        val color = if (activeColor == Color.BLACK) levelColor else activeColor
        view.setBackgroundColor(color)

        canvas.save();
        canvas.translate(left.toFloat(), top.toFloat())
        view.draw(canvas);
        canvas.restore();
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("x", pos.x)
            put("y", pos.y)
            put("level", level)
        }
    }

    override fun toString(): String {
        return "GridTile(value=$value, pos=[${pos.x}, ${pos.y}, level=$level, size=$size])"
    }

    public override fun clone() = GridTile(context, value.clone(), pos, level, size)
}