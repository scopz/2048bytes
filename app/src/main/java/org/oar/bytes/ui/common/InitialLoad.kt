package org.oar.bytes.ui.common

import android.app.Activity
import android.graphics.Color
import org.oar.bytes.R
import org.oar.bytes.utils.Constants
import org.oar.bytes.utils.NumbersExt.color
import org.oar.bytes.utils.ScreenProperties

object InitialLoad {
    fun loadData(activity: Activity) {
        ScreenProperties.load(activity)

        Constants.SHADE_COLORS.clear()
        listOf(
            R.color.shade01.color(activity),
            R.color.shade02.color(activity),
            R.color.shade03.color(activity),
            R.color.shade04.color(activity),
            R.color.shade05.color(activity),
            R.color.shade06.color(activity),
            R.color.shade07.color(activity),
            R.color.shade08.color(activity),
            R.color.shade09.color(activity),
            R.color.shade10.color(activity),
            R.color.shade11.color(activity),
            R.color.shade12.color(activity),
            R.color.shade13.color(activity),
            R.color.shade14.color(activity),
        ).also { Constants.SHADE_COLORS.addAll(it) }

        Constants.BUMP_COLORS.clear()
        listOf(
            R.color.bump01.color(activity),
            R.color.bump02.color(activity),
            R.color.bump03.color(activity),
            R.color.bump04.color(activity),
            R.color.bump05.color(activity),
            R.color.bump06.color(activity),
            R.color.bump07.color(activity),
            Color.BLACK
        ).also { Constants.BUMP_COLORS.addAll(it) }
    }
}