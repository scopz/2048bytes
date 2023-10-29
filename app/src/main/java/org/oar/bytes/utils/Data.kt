package org.oar.bytes.utils

import org.oar.bytes.model.Device
import org.oar.bytes.model.EnergyDevice
import org.oar.bytes.model.SByte
import org.oar.bytes.utils.NumbersExt.sByte

object Data {
    var gridLevel = 1
    var getBytes = { 0.sByte  }
    var consumeBytes = { _: SByte -> false }

    val devices = mutableListOf<Device>()
    val energyDevices = mutableListOf<EnergyDevice>()
}