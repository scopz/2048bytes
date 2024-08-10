package org.oar.bytes.ui.fragments

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import org.json.JSONObject
import org.oar.bytes.ui.GridActivity

abstract class MainView(
    context: Context,
    attrs: AttributeSet? = null,
    @LayoutRes private val layoutId: Int
) : FrameLayout(context, attrs) {

    protected val activity = context as GridActivity
    lateinit var switchView: (Int) -> Unit

    init {
        inflate(context, layoutId, this)
    }

    open fun onCreate() { }
    open fun onPause() { }
    open fun onResume() { }
    open fun onFocus() { }
    open fun onBlur() { }
    open fun onBack(): Boolean = true

    open fun newGame() { }
    open fun fromJson(json: JSONObject) { }
    open fun appendToJson(json: JSONObject) { }
    open fun scheduleNotifications() { }
}