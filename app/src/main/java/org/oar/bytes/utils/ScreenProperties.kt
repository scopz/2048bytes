package org.oar.bytes.utils

import android.app.Activity

object ScreenProperties {
    var DPI = 0f
        private set

    var FRAME_RATE = 0f
        private set

    fun load(activity: Activity) {
        val displayMetrics = activity.resources.displayMetrics
        val dpiW = displayMetrics.xdpi
        val dpiH = displayMetrics.ydpi
        DPI = (dpiW + dpiH) / 2f
        FRAME_RATE = activity.display?.refreshRate ?: 60f
    }
}