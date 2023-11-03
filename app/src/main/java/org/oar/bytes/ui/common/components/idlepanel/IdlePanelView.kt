package org.oar.bytes.ui.common.components.idlepanel

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import org.json.JSONObject
import org.oar.bytes.R
import org.oar.bytes.features.time.TimeControlled
import org.oar.bytes.features.time.TimeController
import org.oar.bytes.model.SByte
import org.oar.bytes.utils.ComponentsExt.runOnUiThread
import org.oar.bytes.utils.Constants.SPEED_TIME_REGENERATE
import org.oar.bytes.utils.JsonExt.getIntOrNull
import org.oar.bytes.utils.NumbersExt.sByte
import org.oar.bytes.utils.NumbersExt.toHHMMSS
import org.oar.bytes.utils.TriConsumer

class IdlePanelView(
    context: Context,
    attr: AttributeSet? = null
) : FrameLayout(context, attr), TimeControlled {

    private var onGoing = false
    private var thread: IdleTick? = null

    var maxTime = 0
    private var currentTime = 0
        set(value) {
            field = if (value > maxTime) maxTime else value
            timeText.text = field.toHHMMSS()
        }
    var bytesSec = 0.sByte
        set(value) {
            field = value
            @SuppressLint("SetTextI18n")
            speedText.text = "$value/s"
        }

    private val timeText by lazy { findViewById<TextView>(R.id.time) }
    private val speedText by lazy { findViewById<TextView>(R.id.speed) }

    private var onClickTimeListener: OnClickListener? = null
    fun setOnClickTimeListener(listener: OnClickListener) { onClickTimeListener = listener }

    private var onClickSpeedListener: OnClickListener? = null
    fun setOnClickSpeedListener(listener: OnClickListener) { onClickSpeedListener = listener }

    private var onProduceByteListener: TriConsumer<Int, SByte, Boolean>? = null
    fun setOnProduceByteListener(listener: TriConsumer<Int, SByte, Boolean>) { onProduceByteListener = listener }

    init {
        LayoutInflater.from(context).inflate(R.layout.component_idle_panel, this, true)
        TimeController.register(0, 0, this)
        TimeController.startTimeData(0, 0, true)
        startTimer()

        currentTime = 0
        bytesSec = 0.sByte

        findViewById<View>(R.id.timeButton).setOnClickListener { onClickTimeListener?.onClick(it) }
        findViewById<View>(R.id.speedButton).setOnClickListener { onClickSpeedListener?.onClick(it) }
    }

    override fun notifyOfflineTime(
        newStartTime: Long,
        relShutdownTime: Long,
        timePassed: Long
    ): Boolean {
        val secs = (timePassed / 1000).toInt().coerceAtMost(currentTime)
        currentTime -= secs + SPEED_TIME_REGENERATE
        onProduceByteListener?.accept(secs, bytesSec * secs.sByte, true)
        return true
    }

    fun stopTimer() {
        thread?.apply {
            interrupt()
            onGoing = false
            thread = null
        }
    }

    fun startTimer() {
        onGoing = true
        if (thread == null) {
            thread = IdleTick().apply { start() }
        }
    }

    inner class IdleTick: Thread() {
        override fun run() {
            val time = 1000L

            try {
                while (onGoing) {
                    val initTime = System.nanoTime()
                    runOnUiThread {
                        onProduceByteListener?.accept(1, bytesSec, false)
                        currentTime += SPEED_TIME_REGENERATE
                    }
                    val endTime = System.nanoTime()

                    val sleepTime = (time - (endTime - initTime) / 1000000)
                    if (sleepTime > 0) {
                        sleep(sleepTime)
                    }
                }
            } catch (_: InterruptedException) {
            }
        }
    }

    fun appendToJson(json: JSONObject) {
        json.apply {
            put("idleTime", currentTime)
        }
    }

    fun fromJson(json: JSONObject) {
        currentTime = json.getIntOrNull("idleTime") ?: 0
    }
}