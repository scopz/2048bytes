package org.oar.bytes.utils

import android.graphics.Color

object Constants {

    const val STATE_FILENAME = "state.save"

    val SCALE_LETTER = listOf(
        "", "K", "M", "G", "T", "P", "E", "Z", "Y", "R", "Q"
    )

    val SCALE_BYTE_LETTER = SCALE_LETTER.map { "${it}B" }

    val SHADE_COLORS = mutableListOf<Int>()
    val BUMP_COLORS = mutableListOf(Color.BLACK)

    /*
    val LEVEL_EXP = listOf(
        300,
        3000,
        15000,
        40000,
        100000,
        233900,
        500000,
        1000000,
        2150000,
    )
    */
    val LEVEL_EXP = listOf(
        100,
        300,
        1000,
        4000,
        10000,
        23390,
        50000,
        100000,
        215000,
        500000,
    )
}