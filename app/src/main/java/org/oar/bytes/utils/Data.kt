package org.oar.bytes.utils

import org.oar.bytes.model.Device
import org.oar.bytes.model.EnergyDevice

object Data {
    var gridLevel = 1

    val devices = mutableListOf<Device>()
    val energyDevices = mutableListOf<EnergyDevice>()
}