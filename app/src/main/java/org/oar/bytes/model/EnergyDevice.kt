package org.oar.bytes.model

data class EnergyDevice(
    val id: Int,
    val capacity: Int,
    val name: String,
    val subDeviceName: String,
    val unlockFee: Int,
    val upgradeFee: SByte,
    val upgradeSubDeviceFee: Int,
    val subDevicePercent: Int
)