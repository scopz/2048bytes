package org.oar.bytes.ui.animations

import android.graphics.Color
import org.oar.bytes.features.animate.Animation
import org.oar.bytes.ui.common.components.grid.GridTile
import org.oar.bytes.utils.Constants.BUMP_COLORS

class BumpTileAnimation(
    private val tile: GridTile
) : Animation {
    override val ref = tile
    override val blockingGrid = false

    private var activeColor = BUMP_COLORS.first()

    override fun startAnimation() {
        activeColor = BUMP_COLORS.first()
    }

    override fun nextAnimation(): Boolean {
        if (activeColor != BUMP_COLORS.last()) {
            activeColor = BUMP_COLORS[BUMP_COLORS.indexOf(activeColor)+1]
        }
        if (activeColor == BUMP_COLORS.last()) {
            return false
        }
        tile.background = activeColor
        return true
    }

    override fun endAnimation() {
        tile.background = Color.BLACK
    }

    override fun applyAnimation() {
        tile.postInvalidate()
    }
}