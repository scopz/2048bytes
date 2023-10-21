package org.oar.bytes.model

data class Device(
    val id: Int,
    val speed: SByte,
    val name: String,
    val subDeviceName: String,
    val unlockFee: Int,
    val upgradeFee: SByte,
    val upgradeSubDeviceFee: Int,
    val subDevicePercent: Int
)