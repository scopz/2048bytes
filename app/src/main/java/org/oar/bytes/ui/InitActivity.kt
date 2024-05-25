package org.oar.bytes.ui

import android.content.Intent
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.oar.bytes.features.notification.NotificationService
import org.oar.bytes.model.Device
import org.oar.bytes.model.EnergyDevice
import org.oar.bytes.utils.Data
import org.oar.bytes.utils.JsonExt.mapJsonObject
import org.oar.bytes.utils.NumbersExt.sByte

class InitActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        loadAssets()
        NotificationService.configureChannels(this)

        val intent = Intent(this, GridActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun loadAssets() {
        Data.devices.clear()
        assetJsonArrayFile("devices.json")
            .mapJsonObject {
                if (it.has("disable")) return@mapJsonObject null
                Device(
                    it.getInt("id"),
                    it.getString("speed").sByte,
                    it.getString("name"),
                    it.getString("upgrade").sByte,
                )
            }
            .filterNotNull()
            .also { Data.devices.addAll(it) }

        Data.energyDevices.clear()
        assetJsonArrayFile("energies.json")
            .mapJsonObject {
                if (it.has("disable")) return@mapJsonObject null
                EnergyDevice(
                    it.getInt("id"),
                    it.getInt("capacity"),
                    it.getString("name"),
                    it.getString("sub-device"),
                    it.getInt("unlock-fee"),
                    it.getString("upgrade").sByte,
                    it.getInt("upgrade-sub-device"),
                    it.getInt("percent-upgrade"),
                )
            }
            .filterNotNull()
            .also { Data.energyDevices.addAll(it) }
    }

    private fun assetJsonArrayFile(fileName: String): JSONArray {
        val file = assets.open(fileName)
        val formArray = ByteArray(file.available())
        file.read(formArray)
        file.close()
        return JSONArray(String(formArray))
    }
}