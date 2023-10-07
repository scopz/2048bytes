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
    private var value: BigInteger = 0.toBigInteger()
): Cloneable {
    companion object {
        val decimalFormat = DecimalFormat("0.##").apply {
            roundingMode = RoundingMode.HALF_UP
        }
    }

    override fun toString(): String {
        var index = 0
        var currentVal = value.toBigDecimal()

        while(currentVal.canReduceScale()) {
            currentVal = currentVal.reduceScale()
            index++
        }

        return "${decimalFormat.format(currentVal)} ${SCALE_BYTE_LETTER[index]}"
    }

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return value == (other as SByte).value
    }

    public override fun clone() = value.sByte
    override fun hashCode() = value.hashCode()
    operator fun plus(byte: SByte) = SByte(value.add(byte.value))
    operator fun minus(byte: SByte) = SByte(value.subtract(byte.value))
    operator fun times(byte: SByte) = SByte(value.multiply(byte.value))
    operator fun compareTo(byte: SByte) = value.compareTo(byte.value)
}