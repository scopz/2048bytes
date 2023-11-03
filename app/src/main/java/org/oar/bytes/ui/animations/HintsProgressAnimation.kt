package org.oar.bytes.ui.animations

import org.oar.bytes.features.animate.Animation
import org.oar.bytes.ui.common.components.hints.HintsView
import org.oar.bytes.utils.NumbersExt.sByte
import kotlin.math.PI
import kotlin.math.sin

class HintsProgressAnimation(
    private val panelView: HintsView,
    secondsToAdd: Int,
    private val duration: Int,
) : Animation {
    override val ref = panelView
    override val blockingGrid = false

    private val diffs = panelView.hints.map {
        (secondsToAdd + it.currentValue).coerceAtMost(it.maxValue) - it.currentValue
    }
    private var secondsAdded = List(diffs.size) { 0 }

    private var startTime = 0L
    private var endTime = duration.toLong()

    override fun startAnimation() {
        startTime = System.currentTimeMillis()
        endTime = startTime + duration
        secondsAdded = List(diffs.size) { 0 }
    }

    private fun easeInOutCubic(x: Float) = sin((x - 0.5) * PI) / 2 + 0.5

    override fun nextAnimation(): Boolean {
        val x = (System.currentTimeMillis() - startTime) / duration.toFloat()
        if (x > 1) return false

        val nx = easeInOutCubic(x)

        secondsAdded = panelView.hints.indices.map { i ->
            val diffPart = (diffs[i] * nx - secondsAdded[i]).toInt()
            panelView.addProgress(diffPart, i)
            secondsAdded[i] + diffPart
        }
        return true
    }

    override fun applyAnimation() {}

    override fun endAnimation() {
        panelView.hints.indices.forEach { i ->
            panelView.addProgress(diffs[i] - secondsAdded[i], i)
        }
    }
}