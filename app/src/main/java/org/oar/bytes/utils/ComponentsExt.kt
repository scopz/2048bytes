package org.oar.bytes.utils

import android.app.Activity
import android.view.View
import androidx.fragment.app.Fragment

object ComponentsExt {
    fun Fragment.runOnUiThread(action: Runnable) = requireActivity().runOnUiThread(action)
    fun View.runOnUiThread(action: Runnable) = (context as Activity).runOnUiThread(action)

    val View.calculatedHeight: Int
        get() = layoutParams
            ?.height
            ?.takeIf { it > 0 }
            ?: run {
                measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                measuredHeight
            }
}
