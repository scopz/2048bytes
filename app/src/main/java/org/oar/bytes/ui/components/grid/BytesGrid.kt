package org.oar.bytes.ui.components.grid

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import org.oar.bytes.model.Position
import org.oar.bytes.utils.NumbersExt.sByte

class BytesGrid(
    context: Context,
    attr: AttributeSet? = null
) : View(context, attr) {

    private var tileSize: Int = 0
    private val tiles = mutableListOf<GridTile>()

    init {
        setBackgroundColor(Color.RED)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = measuredWidth
        tileSize = width / 4
        setMeasuredDimension(width, width)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        tiles.add(GridTile(2099.sByte, Position(1, 2)))
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
        //this.postInvalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            tiles.forEach { it.draw(canvas, tileSize) }
        }
    }
}