package org.oar.bytes.ui.animations

import org.oar.bytes.features.animate.Animation

class WaitAnimation(
    private val value: Long,
    block: Boolean = false
) : Animation(value, block) {

    private var freeTime = value

    override fun startAnimation() {
        freeTime = System.currentTimeMillis() + value
    }

    override fun nextAnimation() = System.currentTimeMillis() < freeTime

    override fun applyAnimation() {}
}