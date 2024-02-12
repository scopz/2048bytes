package org.oar.bytes.utils

import android.graphics.Color
import org.oar.bytes.utils.NumbersExt.sByte
import java.math.BigDecimal

object Constants {

    const val STATE_FILENAME = "state.save"

    const val SPEED_TIME_REGENERATE = 3600

    val SCALE_LETTER = listOf(
        "", "K", "M", "G", "T", "P", "E", "Z", "Y", "R", "Q"
    )

    val SCALE_BYTE_LETTER = SCALE_LETTER.map { "${it}B" }

    val SHADE_COLORS = mutableListOf<Int>()
    val BUMP_COLORS = mutableListOf(Color.BLACK)

    val LEVEL_EXP = GeneratorMap(
        0 to 100.sByte,
        1 to 300.sByte,
    ) { level, prevExp ->

        val factor = when {
            level <= 6 -> "3"
            level <= 7 -> "2.5"
            level <= 25 -> "2.10"
            level <= 45 -> "2.05"
            level <= 60 -> "2.01"
            level <= 70 -> "2.05"
            else -> "2.10"
        }

        prevExp.value.toBigDecimal()
            .multiply(BigDecimal(factor))
            .sByte
    }

    class GeneratorMap<T>(
        vararg pairs: Pair<Int, T>,
        private val generator: (Int, T) -> T
    ) : HashMap<Int, T>() {

        init {
            putAll(pairs)
        }

        override operator fun get(key: Int): T {
            return super.get(key) ?: run {
                val prevValue = this[key - 1]
                generator(key, prevValue)
                    .also { this[key] = it }
            }
        }
    }
}