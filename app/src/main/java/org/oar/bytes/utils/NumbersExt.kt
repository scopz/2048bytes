package org.oar.bytes.utils

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

    fun BigInteger.doubleValue() = this.multiply(TWO)
    fun BigInteger.halveValue() = this.divide(TWO)
    fun BigInteger.doubleValue(times: Int) = this.multiply(TWO.pow(times))
    fun BigInteger.halveValue(times: Int) = this.divide(TWO.pow(times))
}