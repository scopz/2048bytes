package org.oar.bytes.model

import org.oar.bytes.utils.NumbersExt.sByte

data class EnergyDevice(
    val id: Int,
    val capacity: Int,
    val name: String,
    val subDeviceName: String,
    val unlockFee: Int,
    val upgradeFee: SByte,
    val upgradeSubDeviceFee: Int,
    val subDevicePercent: Int
) {
    fun cost(level: Int): SByte {
        return upgradeFee * (level + 1).sByte
    }
}