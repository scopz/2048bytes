package org.oar.bytes.features.animate

import org.oar.bytes.utils.ScreenProperties.FRAME_RATE
import kotlin.math.roundToLong

object Animator {
    private val animations = mutableListOf<AnimationWrapper>()
    private var thread: Framer? = null

    fun addAndStart(animation: Animate) {
        AnimationWrapper(
            System.currentTimeMillis(),
            animation
        ).also {
            animations.add(it)
        }

        animation.start()
        if (thread == null) {
            thread = Framer().apply { start() }
        }
    }

    private fun update() {
        val currentTime = System.currentTimeMillis()
        animations.removeIf {
            val moment = currentTime - it.startTime
            val remove = !it.animation.updateAnimation(moment)
            if (remove) {
                it.animation.end(moment)
            }
            remove
        }
    }

    private data class AnimationWrapper(
        val startTime: Long,
        val animation: Animate
    )

    private class Framer : Thread() {
        override fun run() {
            val time = 1000 / FRAME_RATE

            try {
                while (animations.isNotEmpty()) {
                    val initTime = System.nanoTime()
                    update()
                    val endTime = System.nanoTime()

                    val sleepTime = (time - (endTime - initTime) / 1000000).roundToLong()
                    if (sleepTime > 0) {
                        sleep(sleepTime)
                    }
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            thread = null
        }
    }
}
