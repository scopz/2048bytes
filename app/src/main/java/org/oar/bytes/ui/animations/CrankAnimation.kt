package org.oar.bytes.ui.animations

import org.oar.bytes.features.animate.Animation
import org.oar.bytes.ui.animations.CrankAnimation.Status.POWERING
import org.oar.bytes.ui.animations.CrankAnimation.Status.PRE_SET
import org.oar.bytes.ui.animations.CrankAnimation.Status.PRE_STOPPING
import org.oar.bytes.ui.animations.CrankAnimation.Status.STOPPED
import org.oar.bytes.ui.animations.CrankAnimation.Status.STOPPING
import org.oar.bytes.ui.common.components.crank.CrankView
import org.oar.bytes.utils.ScreenProperties.FRAME_RATE

/**
 * mSlowness: time to reach 1 speed
 * mMaxSpeed: loops per second
 */
class CrankAnimation(
    view: CrankView,
    private val increaseSlowness: Float,
    private val maxSpeed: Float
) : Animation(view, false) {

    companion object {
        const val DECREASE_SLOWNESS = 32f
        const val FREEZE_TIMEOUT_MS = 6000
    }

    var angle = 0f
    private var startTime = 0L
    private var stopTime = 0L
    var speed = 0f
    private var brake = 0f

    var status = STOPPED
        private set

    lateinit var onCycle: () -> Unit
    lateinit var onStatsChange: (Float, Float, Float) -> Unit

    override fun startAnimation() {
        when (status) {
            POWERING -> return
            PRE_SET -> {
                val seconds = speed * increaseSlowness * 1000
                startTime = System.currentTimeMillis() - seconds.toLong()
                return
            }
            STOPPED -> startTime = System.currentTimeMillis()
            else -> {
                val seconds = speed * increaseSlowness * 1000
                startTime = System.currentTimeMillis() - seconds.toLong()
            }
        }
        status = POWERING
    }

    override fun nextAnimation(): Boolean {
        if (status == PRE_STOPPING || status == PRE_SET) {
            status = STOPPING
            stopTime = System.currentTimeMillis()
            brake = 0f
        }

        return when (status) {
            POWERING -> {
                val x = (System.currentTimeMillis() - startTime) / (increaseSlowness * 1000)
                speed = x.coerceAtMost(maxSpeed)
                true
            }
            STOPPING -> {
                val time = System.currentTimeMillis() - stopTime - FREEZE_TIMEOUT_MS
                if (time > 0) {
                    val x = time / (DECREASE_SLOWNESS * 1000)
                    speed -= (x - brake).also { brake += it }
                    speed > 0
                } else true
            }
            else -> false
        }
    }

    override fun endAnimation() {
        status = STOPPED
        speed = 0f
        brake = 0f
        startTime = 0L
        stopTime = 0L
    }

    override fun applyAnimation() {
        angle += speed * 360f / FRAME_RATE
        while (angle >= 360) {
            onCycle()
            angle -= 360
        }
        onStatsChange(angle, speed, maxSpeed)
    }

    fun stop() {
        status = PRE_STOPPING
    }

    fun statusPreset() {
        status = PRE_SET
    }

    enum class Status {
        POWERING,
        PRE_STOPPING,
        STOPPING,
        STOPPED,
        PRE_SET,
    }
}