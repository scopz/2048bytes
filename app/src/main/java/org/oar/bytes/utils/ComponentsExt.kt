package org.oar.bytes.utils

import android.app.Activity
import android.view.View
import androidx.fragment.app.Fragment
import org.oar.bytes.R

object ComponentsExt {
    fun Fragment.runOnUiThread(action: Runnable) = requireActivity().runOnUiThread(action)
    fun View.runOnUiThread(action: Runnable) = (context as Activity).runOnUiThread(action)

    fun Activity.overridePendingSideTransition(toLeft: Boolean = true) {
        if (toLeft) {
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        } else {
            overridePendingTransition(R.anim.slide_in_left_back, R.anim.slide_out_right_back)
        }
    }
}