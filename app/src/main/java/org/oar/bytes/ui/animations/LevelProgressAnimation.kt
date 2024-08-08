package org.oar.bytes.ui.animations

import org.oar.bytes.features.animate.Animation
import org.oar.bytes.model.SByte
import org.oar.bytes.ui.common.components.levelpanel.LevelPanelView
import org.oar.bytes.utils.NumbersExt.sByte
import kotlin.math.PI
import kotlin.math.sin

class LevelProgressAnimation(
    private val panelView: LevelPanelView,
    bytesToAdd: SByte,
    private val duration: Int,
) : Animation {
    override val ref = panelView
    override val blockingGrid = false

    private val initValue = panelView.storedValue.value
    private val diff = (bytesToAdd + initValue).coerceAtMost(panelView.capacity) - initValue
    private var added = 0.sByte

    private var startTime = 0L
    private var endTime = duration.toLong()

    override fun startAnimation() {
        startTime = System.currentTimeMillis()
        endTime = startTime + duration
        added = 0.sByte
    }

    private fun easeInOutCubic(x: Float) = sin((x - 0.5) * PI) / 2 + 0.5

    override fun nextAnimation(): Boolean {
        val x = (System.currentTimeMillis() - startTime) / duration.toFloat()
        if (x > 1) return false

        val nx = easeInOutCubic(x).toBigDecimal()

        val diffPart = diff.value.toBigDecimal().multiply(nx).sByte - added
        added += diffPart

        panelView.addBytes(diffPart, true)
        return true
    }

    override fun applyAnimation() {}

    override fun endAnimation() {
        panelView.addBytes(diff - added, true)
        panelView.storedValue.clearFinal()
    }
}