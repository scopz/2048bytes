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
    ) {

        fun generateNumbers(start: String, factor: String, power: Int) = BigDecimal(start)
            .multiply(BigDecimal(factor).pow(power))
            .toBigInteger()
            .sByte

        when(it) {
            in 0 until 6 -> generateNumbers("100", "3", it)
            in 6 until 25 -> generateNumbers("72900", "2.1", it - 6)
            in 25 until 40 -> generateNumbers("96581540508", "2.05", it - 25)
            else -> generateNumbers("4583550748000000", "2.01", it - 40)
        }
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