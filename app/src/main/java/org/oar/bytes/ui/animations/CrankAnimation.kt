package org.oar.bytes.ui.animations

import org.oar.bytes.features.animate.Animation
import org.oar.bytes.ui.animations.CrankAnimation.Status.POWERING
import org.oar.bytes.ui.animations.CrankAnimation.Status.PRE_STOPPING
import org.oar.bytes.ui.animations.CrankAnimation.Status.STOPPED
import org.oar.bytes.ui.animations.CrankAnimation.Status.STOPPING
import org.oar.bytes.ui.common.components.crank.CrankView
import kotlin.reflect.KFunction1

class CrankAnimation(
    private val mCrankView: CrankView,
    private val mSlowness: Float,
    private val mMaxSpeed: Float,
    private val runOnUiThread: KFunction1<Runnable, Unit>,
) : Animation {
    companion object {
        private const val MINIMUM_MAX_SPEED = 10f
    }
    override val ref = mCrankView
    override val blockingGrid = false

    private var mAngle = 0f
    private var startTime = 0L
    private var stopTime = 0L
    private var speed = 0f
    private var brake = 0f

    var status = STOPPED
        private set

    lateinit var onCycle: () -> Unit
    lateinit var onAngleChange: (Float) -> Unit

    override fun startAnimation() {
        startTime = when (status) {
            POWERING -> return
            STOPPED -> System.currentTimeMillis()
            else -> {
                val seconds = (speed / mMaxSpeed) * mSlowness * 1000
                System.currentTimeMillis() - seconds.toLong()
            }
        }
        status = POWERING
    }

    override fun nextAnimation(): Boolean {
        if (status == PRE_STOPPING) {
            status = STOPPING
            stopTime = System.currentTimeMillis()
            brake = 0f
        }

        return when (status) {
            POWERING -> {
                val x = (System.currentTimeMillis() - startTime) / (mSlowness * 1000)
                speed = (MINIMUM_MAX_SPEED * x).coerceAtMost(mMaxSpeed)
                true
            }
            STOPPING -> {
                val x = (System.currentTimeMillis() - stopTime) / (mSlowness * 1000)
                speed -= ((MINIMUM_MAX_SPEED * x) - brake).also { brake += it }
                speed > 0
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
        mAngle += speed
        while (mAngle >= 360) {
            onCycle()
            mAngle -= 360
        }
        onAngleChange(mAngle)
        runOnUiThread {
            mCrankView.rotation = mAngle
        }
    }

    fun stop() {
        status = PRE_STOPPING
    }

    enum class Status {
        POWERING,
        PRE_STOPPING,
        STOPPING,
        STOPPED
    }
}