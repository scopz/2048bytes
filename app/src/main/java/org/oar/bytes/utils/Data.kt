package org.oar.bytes.utils

import org.oar.bytes.model.Device
import org.oar.bytes.model.EnergyDevice
import org.oar.bytes.model.SByte
import org.oar.bytes.model.UiValue

object Data {
    var gameLevel = UiValue(0)
    var capacity = UiValue(SByte.ZERO)
    val bytes = UiValue(SByte.ZERO)
        .apply {
            limit = { it.coerceIn(SByte.ZERO, capacity.value) }
        }

    val devices = mutableListOf<Device>()
    val energyDevices = mutableListOf<EnergyDevice>()

    fun consumeBytes(bytesToConsume: SByte) =
        if (bytesToConsume.isBiggerThanZero) {
            if (bytes.value >= bytesToConsume) {
                bytes.operate { it - bytesToConsume }
                true
            } else {
                false
            }
        }
        else if (bytesToConsume.isZero) true
        else false
}