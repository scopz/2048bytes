package org.oar.bytes.utils.extensions

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment

@Suppress("UNCHECKED_CAST")
object ComponentsExt {
    fun Fragment.runOnUiThread(action: Runnable) = requireActivity().runOnUiThread(action)
    fun View.runOnUiThread(action: Runnable) = (context as Activity).runOnUiThread(action)

    fun <T: Any> Activity.getService(name: String): T = getSystemService(name) as T
    fun <T: Any> Context.getService(name: String): T = getSystemService(name) as T

    val View.calculatedHeight: Int
        get() = layoutParams
            ?.height
            ?.takeIf { it > 0 }
            ?: run {
                measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                measuredHeight
            }
}
