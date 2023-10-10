package org.oar.bytes.ui.animations

import org.oar.bytes.features.animate.Animation
import org.oar.bytes.model.Position
import org.oar.bytes.ui.common.components.grid.GridTile
import kotlin.math.abs

class MoveTileAnimation(
    private val tile: GridTile,
    destiny: Position,
    private val speed: Int
) : Animation {
    override val ref = tile

    private var pointX: Int = tile.pos.x * tile.size
    private var pointY: Int = tile.pos.y * tile.size

    private var destinyPointX: Int = destiny.x * tile.size
    private var destinyPointY: Int = destiny.y * tile.size

    override fun startAnimation() {
        tile.pointX = pointX
        tile.pointY = pointY
    }

    override fun nextAnimation(): Boolean {

        fun resolveNextPoint(base: Int, destiny: Int): Int {
            val dx = destiny - base
            return if (abs(dx) < speed)
                destiny
            else if (dx > 0)
                base + speed
            else
                base - speed
        }

        if (pointY == destinyPointY) {
            pointX = resolveNextPoint(pointX, destinyPointX)
            tile.pointX = pointX
            return pointX != destinyPointX
        }

        if (pointX == destinyPointX) {
            pointY = resolveNextPoint(pointY, destinyPointY)
            tile.pointY = pointY
            return pointY != destinyPointY
        }

        return false
    }

    override fun applyAnimation() {
        tile.postInvalidate()
    }
}