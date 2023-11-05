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
        2 to 3000.sByte,
        3 to 15000.sByte,
        4 to 40000.sByte,
        5 to 100000.sByte,
        6 to 240000.sByte,
        7 to 600000.sByte,
    ) {
        BigDecimal("3906.25")
            .multiply(BigDecimal("2.1").pow(it))
            .toBigInteger()
            .sByte
            .also { a -> println(a.value) }
    }

    class GeneratorMap<T>(
        vararg pairs: Pair<Int, T>,
        private val generator: (Int) -> T
    ) : HashMap<Int, T>() {

        init {
            putAll(pairs)
        }

        override operator fun get(key: Int): T {
            return super.get(key) ?: run {
                generator(key).also { this[key] = it }
            }
        }
    }
}