package org.oar.bytes.ui.common.components.hints

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import org.oar.bytes.R
import org.oar.bytes.model.UiValue
import org.oar.bytes.ui.common.LimitedDrawFrameLayout
import org.oar.bytes.utils.ColoredRect
import org.oar.bytes.utils.ColoredRect.Companion.drawRect
import org.oar.bytes.utils.extensions.ComponentsExt.runOnUiThread
import org.oar.bytes.utils.extensions.NumbersExt.color
import org.oar.bytes.utils.extensions.NumbersExt.toDynamicHHMMSS

class HintButtonView(
    context: Context,
    attrs: AttributeSet? = null
) : LimitedDrawFrameLayout(context, attrs) {

    private var setOnReadyListener: Runnable? = null
    fun setOnReadyListener(listener: Runnable) { setOnReadyListener = listener }

    private var onClickListener: OnClickListener? = null
    override fun setOnClickListener(listener: OnClickListener?) { onClickListener = listener }

    var ready = false
        private set

    private var progress = 0f
        set(value) {
            field = if (value >= 1) 1f else value
            updateButtonBar()
            postInvalidate()
        }

    var secondsToLoad = 1
    val seconds = UiValue(0)
        .apply {
            observe(context as LifecycleOwner) { updateUi() }
        }

    private val progressRect = ColoredRect(context, this, R.color.hintsColor)

    private val imageView by lazy { findViewById<ImageView>(R.id.image) }
    private val timeView by lazy { findViewById<TextView>(R.id.time) }

    var active: Boolean = false
        set(value) {
            field = value
            if (value)
                imageView.setColorFilter(R.color.activeHintColor.color(context))
            else
                imageView.colorFilter = null
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.component_hints_button, this, true)

        attrs
            ?.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "src", id)
            .also {
                if (it == null || it == id) throw RuntimeException("Hud view id not found")
                imageView.setImageResource(it)
            }

        super.setOnClickListener {
            if (ready) {
                onClickListener?.onClick(it)
            }
        }
    }


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        progressRect.bottom = measuredHeight
        progressRect.right = measuredWidth

        updateButtonBar()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        updateButtonBar()
    }

    fun flushFinalSeconds() {
        seconds.apply {
            operate(true) { finalValue }
            clearFinal()
        }
    }

    fun addSeconds(currentSeconds: Int, ignoreFuture: Boolean = false) {
        seconds.operate(ignoreFuture) {
            (currentSeconds + it).coerceAtMost(secondsToLoad)
        }
    }

    private fun setSeconds(currentSeconds: Int) {
        seconds.operate(false) { currentSeconds.coerceAtMost(secondsToLoad) }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(progressRect)
    }

    private fun updateButtonBar() {
        if (progress >= 1) {
            alpha = if (isEnabled) 1f else 0.5f
            progressRect.percentBottomHeight = 0f

            if (!ready) {
                ready = true
                setOnReadyListener?.run()
            }
        } else {
            ready = false
            alpha = .5f
            progressRect.percentBottomHeight = progress
        }
    }

    private fun updateUi() {
        runOnUiThread {
            progress = seconds.value / secondsToLoad.toFloat()
            timeView.text = (secondsToLoad - seconds.value).toDynamicHHMMSS()
        }
    }

    fun reset() {
        setSeconds(0)
    }

    fun fromJson(seconds: Int) {
        setSeconds(seconds)
    }
}

