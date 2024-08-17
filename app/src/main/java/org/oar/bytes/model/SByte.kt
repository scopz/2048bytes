package org.oar.bytes.model

import org.oar.bytes.utils.Constants.SCALE_BYTE_LETTER
import org.oar.bytes.utils.NumbersExt.canReduceScale
import org.oar.bytes.utils.NumbersExt.doubleValue
import org.oar.bytes.utils.NumbersExt.halveValue
import org.oar.bytes.utils.NumbersExt.reduceScale
import org.oar.bytes.utils.NumbersExt.sByte
import java.math.BigInteger
import java.math.RoundingMode
import java.text.DecimalFormat

data class SByte (
    var value: BigInteger = BigInteger.ZERO
): Cloneable {

    companion object {
        val decimalFormat = DecimalFormat("0.##").apply {
            roundingMode = RoundingMode.HALF_UP
        }
    }

    override fun toString(): String {
        val stringParts = toStringSplit()
        return "${stringParts[0]} ${stringParts[1]}"
    }

    fun toStringSplit(): Array<String> {
        var index = 0
        var currentVal = value.toBigDecimal()

        while(currentVal.canReduceScale()) {
            currentVal = currentVal.reduceScale()
            index++
        }

        return arrayOf(decimalFormat.format(currentVal), SCALE_BYTE_LETTER[index])
    }

    val isZero: Boolean
        get() = value.compareTo(BigInteger.ZERO) == 0

    val isBiggerThanZero: Boolean
        get() = value > BigInteger.ZERO

    val isNegative: Boolean
        get() = value < BigInteger.ZERO

    fun add(byte: SByte) {
        value = (this + byte).value
    }

    fun substract(byte: SByte) {
        value = (this - byte).value
    }

    fun doubleValue() {
        value = value.doubleValue()
    }

    fun doubleValue(times: Int) {
        value = value.doubleValue(times)
    }

    fun halveValue() {
        value = value.halveValue()
    }

    fun halveValue(times: Int) {
        value = value.halveValue(times)
    }

    fun double() = value.doubleValue().sByte
    fun double(times: Int) = value.doubleValue(times).sByte
    fun halve() = value.halveValue().sByte
    fun halve(times: Int) = value.halveValue(times).sByte

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return value.compareTo((other as SByte).value) == 0
    }

    public override fun clone() = value.sByte
    override fun hashCode() = value.hashCode()
    operator fun plus(byte: SByte) = SByte(value.add(byte.value))
    operator fun minus(byte: SByte) = SByte(value.subtract(byte.value))
    operator fun unaryMinus() = SByte(value.negate())
    operator fun times(byte: SByte) = SByte(value.multiply(byte.value))
    operator fun div(byte: SByte) = SByte(value.divide(byte.value))
    operator fun compareTo(byte: SByte) = value.compareTo(byte.value)
    fun coerceAtMost(other: SByte) = value.coerceAtMost(other.value).sByte
}