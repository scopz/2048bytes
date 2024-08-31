package org.oar.bytes.ui.animations

import android.view.View
import org.oar.bytes.features.animate.Animation
import kotlin.math.PI
import kotlin.math.sin
import kotlin.reflect.KFunction1

class ResizeHeightAnimation(
    private val mView: View,
    private val mHeight: Int,
    private val duration: Int,
    private val runOnUiThread: KFunction1<Runnable, Unit>,
) : Animation() {
    override val ref = mView
    override val blockingGrid = true

    private val mStartHeight = mView.height
    private var startTime = 0L

    override fun startAnimation() {
        startTime = System.currentTimeMillis()
    }

    private fun easeInOutCubic(x: Float) = sin((x - 0.5) * PI) / 2 + 0.5

    override fun nextAnimation(): Boolean {
        val x = (System.currentTimeMillis() - startTime) / duration.toFloat()
        if (x > 1) return false

        val newHeight = mStartHeight + ((mHeight - mStartHeight) * easeInOutCubic(x)).toInt()

        if (newHeight == 0) {
            mView.visibility = View.GONE
        } else if (mView.visibility == View.GONE) {
            mView.visibility = View.VISIBLE
        }

        runOnUiThread {
            mView.layoutParams.height = newHeight
            mView.requestLayout()
        }
        return true
    }

    override fun endAnimation() {
        //mView.layoutParams.height = mHeight
    }

    override fun applyAnimation() {
        //mView.requestLayout()
    }
}