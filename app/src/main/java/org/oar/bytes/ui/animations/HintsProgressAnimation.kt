package org.oar.bytes.ui.animations

import org.oar.bytes.features.animate.Animation
import org.oar.bytes.ui.common.components.hints.HintsView
import kotlin.math.PI
import kotlin.math.sin

class HintsProgressAnimation(
    private val panelView: HintsView,
    private val secondsToAdd: Int,
    private val duration: Int,
) : Animation() {
    override val ref = panelView
    override val blockingGrid = false

    private val diffs = panelView.hints.map {
        (secondsToAdd + it.seconds.value).coerceAtMost(it.secondsToLoad) - it.seconds.value
    }
    private var secondsAdded = List(diffs.size) { 0 }

    private var startTime = 0L

    override fun startAnimation() {
        startTime = System.currentTimeMillis()
        secondsAdded = List(diffs.size) { 0 }
    }

    private fun easeInOutCubic(x: Float) = sin((x - 0.5) * PI) / 2 + 0.5

    override fun nextAnimation(): Boolean {
        val x = (System.currentTimeMillis() - startTime) / duration.toFloat()
        if (x > 1) return false

        val nx = easeInOutCubic(x)

        secondsAdded = panelView.hints.mapIndexed { i, button ->
            val diffPart = (diffs[i] * nx - secondsAdded[i]).toInt()
            button.addSeconds(diffPart, true)
            secondsAdded[i] + diffPart
        }
        return true
    }

    override fun applyAnimation() {}

    override fun endAnimation() {
        panelView.hints.mapIndexed { i, button ->
            button.addSeconds(diffs[i] - secondsAdded[i], true)
            button.seconds.clearFinal()
        }
    }
}