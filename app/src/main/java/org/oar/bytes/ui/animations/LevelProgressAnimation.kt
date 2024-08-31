package org.oar.bytes.ui.animations

import android.view.View
import org.oar.bytes.features.animate.Animation
import org.oar.bytes.model.SByte
import org.oar.bytes.utils.Data
import org.oar.bytes.utils.extensions.NumbersExt.sByte
import kotlin.math.PI
import kotlin.math.sin

class LevelProgressAnimation(
    view: View,
    bytesToAdd: SByte,
    private val duration: Int,
) : Animation() {
    override val ref = view
    override val blockingGrid = false

    private val initValue = Data.bytes.value
    private val diff = (bytesToAdd + initValue).coerceAtMost(Data.capacity.value) - initValue
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

        Data.bytes.operate(false) { it + diffPart }
        return true
    }

    override fun applyAnimation() {}

    override fun endAnimation() {
        Data.bytes.operate(false) { it + diff - added }
        Data.bytes.clearFinal()
    }
}