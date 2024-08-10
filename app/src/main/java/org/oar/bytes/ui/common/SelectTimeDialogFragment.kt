package org.oar.bytes.ui.common

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.NumberPicker
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import org.oar.bytes.R
import java.util.function.Consumer

class SelectTimeDialogFragment(
    @StringRes private val title: Int,
    private val startTime: String,
    private val confirm: Consumer<String>,
    private val cancel: Runnable = Runnable {}
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_select_time, null)

        val hourSelector = view.findViewById<NumberPicker>(R.id.hourSelector)
        val minuteSelector = view.findViewById<NumberPicker>(R.id.minuteSelector)

        val (hour, minute) = startTime
            .split(":")
            .map(String::toInt)

        hourSelector.minValue = 0
        hourSelector.maxValue = 23
        hourSelector.value = hour

        minuteSelector.minValue = 0
        minuteSelector.maxValue = 59
        minuteSelector.value = minute

        val builder = AlertDialog.Builder(activity)
        builder.setMessage(title)
            .setView(view)
            .setPositiveButton(R.string.button_confirm) { _, _ ->
                confirm.accept("${hourSelector.value}:${minuteSelector.value}")
            }
            .setNegativeButton(R.string.button_cancel) { _, _ -> cancel.run() }

        return builder.create()
    }
}