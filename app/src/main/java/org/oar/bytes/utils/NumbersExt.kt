package org.oar.bytes.utils

import android.content.Context
import org.oar.bytes.model.SByte
import java.math.BigDecimal
import java.math.BigInteger

object NumbersExt {
    val TWO = 2.toBigInteger()
    val THOUSAND_BYTES = 1024.toBigInteger()
    private val THOUSAND_BYTES_BD = THOUSAND_BYTES.toBigDecimal()

    val BigInteger.sByte: SByte
        get() = SByte(this)

    val Int.sByte: SByte
        get() = this.toBigInteger().sByte

    val Long.sByte: SByte
        get() = this.toBigInteger().sByte

    val String.sByte: SByte
        get() = this.toBigInteger().sByte

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
}