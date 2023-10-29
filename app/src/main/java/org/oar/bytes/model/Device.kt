package org.oar.bytes.model

import org.oar.bytes.utils.NumbersExt.sByte
import kotlin.math.pow

data class Device(
    val id: Int,
    val speed: SByte,
    val name: String,
    val subDeviceName: String,
    val unlockFee: Int,
    val upgradeFee: SByte,
    val upgradeSubDeviceFee: Int,
    val subDevicePercent: Int
) {
    fun cost(level: Int): SByte {
        val multiplier = 1.1.pow(level)
        return upgradeFee.value
            .toBigDecimal()
            .multiply(multiplier.toBigDecimal())
            .toBigInteger().sByte
    }
}