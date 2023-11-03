package org.oar.bytes.ui.common

import android.app.AlertDialog
import android.content.Context
import androidx.annotation.StringRes
import org.oar.bytes.R

object ConfirmDialog {

    fun show(
        context: Context,
        @StringRes title: Int,
        @StringRes message: Int,
        confirm: Runnable,
        cancel: Runnable = Runnable {}
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(R.string.btn_confirm) { _, _ -> confirm.run() }
        builder.setNegativeButton(R.string.btn_cancel) { _, _ -> cancel.run() }
        val dialog = builder.create()
        dialog.show()
    }
}