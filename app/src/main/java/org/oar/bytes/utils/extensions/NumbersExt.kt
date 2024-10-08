package org.oar.bytes.utils.extensions

import android.content.Context
import org.oar.bytes.model.SByte
import org.oar.bytes.utils.Constants.SCALE_LETTER
import java.math.BigDecimal
import java.math.BigInteger

object NumbersExt {
    val TWO = 2.toBigInteger()
    val THOUSAND_BYTES = 1024.toBigInteger()
    private val THOUSAND_BYTES_BD = THOUSAND_BYTES.toBigDecimal()

    val BigInteger.sByte: SByte
        get() = SByte(this)

    val BigDecimal.sByte: SByte
        get() = this.toBigInteger().sByte

    val Int.sByte: SByte
        get() = this.toBigInteger().sByte

    val Long.sByte: SByte
        get() = this.toBigInteger().sByte

    val String.sByte: SByte
        get() {
            if (this.matches(Regex("^\\d+[A-Z]$"))) {
                val letter = this.substring(this.lastIndex)
                val value = this.substring(0, this.lastIndex)
                val scale = SCALE_LETTER.indexOf(letter)
                if (scale > 0) {
                    val nextScale = 1024.sByte
                    return (0 until scale)
                        .fold(value.sByte) { acc, _ -> acc.times(nextScale) }
                }
            }
            return this.toBigInteger().sByte
        }

    fun BigDecimal.canReduceScale() = this >= THOUSAND_BYTES_BD
    fun BigDecimal.reduceScale(): BigDecimal {
        if (!this.canReduceScale()) {
            throw Exception("Cannot reduce scale of $this")
        }
        return this.divide(THOUSAND_BYTES_BD)
    }

    fun BigInteger.doubleValue(): BigInteger = this.multiply(TWO)
    fun BigInteger.halveValue(): BigInteger = this.divide(TWO)
    fun BigInteger.doubleValue(times: Int): BigInteger = this.multiply(TWO.pow(times))
    fun BigInteger.halveValue(times: Int): BigInteger = this.divide(TWO.pow(times))

    fun Int.color(context: Context) = context.getColor(this)

    fun Int.toHHMMSS(): String {
        fun ten(v: Int) = if (v < 10) "0$v" else "$v"
        val s = this % 60
        val m = (this / 60) % 60
        val h = this / 3600
        return "$h:${ten(m)}:${ten(s)}"
    }

    fun Int.toDynamicHHMMSS(): String {
        fun ten(v: Int) = if (v < 10) "0$v" else "$v"
        val s = this % 60
        val m = (this / 60) % 60
        val h = this / 3600
        return if (h != 0)
            "$h:${ten(m)}:${ten(s)}"
        else if (m != 0)
            "$m:${ten(s)}"
        else if (s != 0)
            "$s"
        else
            ""
    }

    fun Int.toMins() = this / 60
}