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
import org.json.JSONObject
import org.oar.bytes.R
import org.oar.bytes.features.animate.AnimationChain
import org.oar.bytes.features.animate.Animator
import org.oar.bytes.features.time.TimeControlled
import org.oar.bytes.features.time.TimeController
import org.oar.bytes.model.SByte
import org.oar.bytes.ui.animations.CrankAnimation
import org.oar.bytes.ui.animations.CrankAnimation.Companion.DECREASE_SLOWNESS
import org.oar.bytes.ui.animations.CrankAnimation.Status.POWERING
import org.oar.bytes.ui.animations.CrankAnimation.Status.PRE_STOPPING
import org.oar.bytes.ui.animations.CrankAnimation.Status.STOPPED
import org.oar.bytes.ui.animations.CrankAnimation.Status.STOPPING
import org.oar.bytes.utils.Data
import org.oar.bytes.utils.extensions.ComponentsExt.runOnUiThread
import org.oar.bytes.utils.extensions.JsonExt.getFloatOrNull
import org.oar.bytes.utils.extensions.NumbersExt.sByte
import kotlin.math.min

@SuppressLint("ClickableViewAccessibility")
class CrankView(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs), TimeControlled {

    private var anim: CrankAnimation
    var onStatsChange: ((Float, Float, Float, SByte) -> Unit)? = null

    private var angle: Float = 0f
        set(value) {
            if (!numb) {
                runOnUiThread {
                    super.setRotation(value)
                }
            }
            field = value
        }

    private var speed = 0f
    var numb = true
        set(value) {
            val redraw = field && !value
            field = value
            if (redraw) angle = angle
        }

    private val bytesToAdd get() = 4.sByte.double(Data.gameLevel.value)

    private val crank by lazy { findViewById<ImageView>(R.id.crank) }

    init {
        LayoutInflater.from(context).inflate(R.layout.component_crank, this, true)
        TimeController.register(0, 1, this)
        TimeController.startTimeData(0, 1, true)

        anim = CrankAnimation(
            this,
            2.5f,
            2f
        ).apply {
            onCycle = {
                Data.bytes.operate { it + bytesToAdd }
            }
        }

        anim.onStatsChange = { angle, speed, mMaxSpeed ->
            this.angle = angle
            this.speed = speed
            this.onStatsChange?.let { it(angle, speed, mMaxSpeed, bytesToAdd) }
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


    fun appendToJson(json: JSONObject) {
        json.apply {
            put("crankAngle", angle)
            put("crankSpeed", speed)
        }
    }

    fun fromJson(json: JSONObject) {
        angle = json.getFloatOrNull("crankAngle") ?: 0f
        speed = json.getFloatOrNull("crankSpeed") ?: 0f
        anim.angle = angle
    }

    override fun notifyOfflineTime(
        newStartTime: Long,
        relShutdownTime: Long,
        timePassed: Long
    ): Boolean {

        val timePassedSeconds = (timePassed / 1000.0)
            .coerceAtMost(DECREASE_SLOWNESS * speed.toDouble())

        val speedToSubtract = (timePassedSeconds / DECREASE_SLOWNESS)
        val newSpeed = (speed - speedToSubtract).coerceAtLeast(0.0)

        val meanSpeed = (speed + newSpeed)/2
        val totalLoops = meanSpeed * timePassedSeconds
        val finalAngle = totalLoops * 360 + angle

        angle = (finalAngle % 360).toFloat()
        anim.angle = angle
        val countingLoops = (finalAngle/360).toInt()

        if (countingLoops > 0) {
            Data.bytes.operate { it + bytesToAdd * countingLoops }
        }

        if (newSpeed > 0) {
            anim.speed = newSpeed.toFloat()
            anim.statusPreset()
            Animator.addAndStart(
                AnimationChain(crank).next { anim }
            )
        }

        return true
    }
}
