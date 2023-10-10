package org.oar.bytes.ui.common.components.grid

import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.widget.TextView
import org.json.JSONObject
import org.oar.bytes.R
import org.oar.bytes.model.Position
import org.oar.bytes.model.SByte
import org.oar.bytes.utils.Constants.SHADE_COLORS

open class GridTile(
    private val parent: View,
    val value: SByte,
    pos_: Position,
    var level: Int,
    val size: Int
): Cloneable {
    private val context = parent.context

    private val levelColor
        get() = when {
            level < SHADE_COLORS.size -> SHADE_COLORS[level]
            else -> SHADE_COLORS.last()
        }

    var pointX = pos_.x * size
    var pointY = pos_.y * size
    var pos = pos_
        set(value) {
            field = value
            pointX = value.x * size
            pointY = value.y * size
        }

    private val view = View.inflate(context, R.layout.component_grid_tile, null)
    private val valueView = view.findViewById<TextView>(R.id.value)

    var background: Int = Color.BLACK
        set(value) {
            field = value
            val color = if (value == Color.BLACK) levelColor else value
            view.setBackgroundColor(color)
        }

    init {
        val measureWidth = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)
        val measuredHeight = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)
        view.measure(measureWidth, measuredHeight)
        view.layout(0, 0, size, size)
        view.setBackgroundColor(levelColor)
        valueView.text = value.toString()
    }

    fun postInvalidate() = parent.postInvalidate()

    fun advanceTileLevel() {
        value.doubleValue()
        valueView.text = value.toString()
        level++
    }

    fun reduceTileLevel() {
        value.halveValue()
        valueView.text = value.toString()
        level--
    }

    fun advancedGridLevel() {
        value.doubleValue()
        valueView.text = value.toString()
    }

    fun draw(canvas: Canvas) {
        canvas.save();
        canvas.translate(pointX.toFloat(), pointY.toFloat())
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

    public override fun clone() = GridTile(parent, value.clone(), pos, level, size)
}