package org.oar.bytes.ui.common.components.crank

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
import android.widget.FrameLayout
import android.widget.ImageView
import org.oar.bytes.R
import org.oar.bytes.features.animate.AnimationChain
import org.oar.bytes.features.animate.Animator
import org.oar.bytes.model.SByte
import org.oar.bytes.ui.animations.CrankAnimation
import org.oar.bytes.ui.animations.CrankAnimation.Status.POWERING
import org.oar.bytes.ui.animations.CrankAnimation.Status.PRE_STOPPING
import org.oar.bytes.ui.animations.CrankAnimation.Status.STOPPED
import org.oar.bytes.ui.animations.CrankAnimation.Status.STOPPING
import org.oar.bytes.utils.ComponentsExt.runOnUiThread
import org.oar.bytes.utils.Data
import org.oar.bytes.utils.NumbersExt.sByte
import kotlin.math.min

@SuppressLint("ClickableViewAccessibility")
class CrankView(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var anim: CrankAnimation
    var onStatsChange: ((Float, Float, Float, SByte) -> Unit)? = null

    private var numbRotation: Float = 0f
        set(value) {
            if (!numb) {
                runOnUiThread {
                    super.setRotation(value)
                }
            }
            field = value
        }

    var numb = true
        set(value) {
            val redraw = field && !value
            field = value
            if (redraw) numbRotation = numbRotation
        }

    private val bytesToAdd get() = 3.sByte.double(Data.gameLevel)

    init {
        LayoutInflater.from(context).inflate(R.layout.component_crank, this, true)

        val crank = findViewById<ImageView>(R.id.crank)

        anim = CrankAnimation(
            this,
            2f,
            2f
        ).apply {
            onStatsChange = { angle, speed, mMaxSpeed ->
                this@CrankView.onStatsChange?.let { it(angle, speed, mMaxSpeed, bytesToAdd) }
            }
            onCycle = { Data.consumeBytes(-bytesToAdd) }
        }

        crank.setOnTouchListener { _, event ->
            when(event.action) {
                ACTION_DOWN -> {
                    when(anim.status) {
                        STOPPED ->
                            Animator.addAndStart(
                                AnimationChain(crank).next { anim }
                            )
                        STOPPING,
                        PRE_STOPPING -> anim.startAnimation()
                        else -> Unit
                    }
                }
                ACTION_MOVE -> {
                    if (anim.status == POWERING) {
                        val x = event.x
                        val y = event.y
                        if (min(x,y) < 0 || y > crank.height || x > crank.width) {
                            anim.stop()
                        }
                    }
                }
                ACTION_UP -> {
                    if (anim.status == POWERING) {
                        anim.stop()
                    }
                }
            }
            true
        }
    }

    override fun setRotation(rotation: Float) {
        numbRotation = rotation
    }
}
