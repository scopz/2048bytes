package org.oar.bytes.ui.common

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import org.oar.bytes.R

class TimePreference(
    context: Context,
    attrs: AttributeSet? = null
) : EditTextPreference(context, attrs) {

    override fun onClick() {
        SelectTimeDialogFragment(
            title = R.string.dialog_time_title,
            startTime = text,
            confirm = {
                text = it
            })
            .show((context as AppCompatActivity).supportFragmentManager, null)
    }
}