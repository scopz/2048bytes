package org.oar.bytes.ui

import android.content.Intent
import android.content.res.AssetManager
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.oar.bytes.R
import org.oar.bytes.features.time.TimeController
import org.oar.bytes.model.Device
import org.oar.bytes.model.EnergyDevice
import org.oar.bytes.ui.common.InitialLoad
import org.oar.bytes.ui.common.components.grid.Grid2048View
import org.oar.bytes.ui.common.components.hints.HintsView
import org.oar.bytes.ui.common.components.idlepanel.IdlePanelView
import org.oar.bytes.ui.common.components.levelpanel.LevelPanelView
import org.oar.bytes.utils.Data
import org.oar.bytes.utils.JsonExt.mapJsonObject
import org.oar.bytes.utils.NumbersExt.sByte
import org.oar.bytes.utils.SaveStateExt.deleteState
import org.oar.bytes.utils.SaveStateExt.hasState
import org.oar.bytes.utils.SaveStateExt.loadState
import org.oar.bytes.utils.SaveStateExt.saveState
import java.io.IOException

class InitActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        loadAssets()

        val intent = Intent(this, GridActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun loadAssets() {
        Data.devices.clear()
        assetJsonArrayFile("devices.json").mapJsonObject {
            Device(
                it.getInt("id"),
                it.getLong("speed").sByte,
                it.getString("name"),
                it.getString("sub-device"),
                it.getInt("unlock-fee"),
                it.getLong("upgrade").sByte,
                it.getInt("upgrade-sub-device"),
                it.getInt("percent-upgrade"),
            )
        }.also { Data.devices.addAll(it) }

        Data.energyDevices.clear()
        assetJsonArrayFile("energies.json").mapJsonObject {
            EnergyDevice(
                it.getInt("id"),
                it.getInt("capacity"),
                it.getString("name"),
                it.getString("sub-device"),
                it.getInt("unlock-fee"),
                it.getLong("upgrade").sByte,
                it.getInt("upgrade-sub-device"),
                it.getInt("percent-upgrade"),
            )
        }.also { Data.energyDevices.addAll(it) }
    }

    private fun assetJsonArrayFile(fileName: String): JSONArray {
        val file = assets.open(fileName)
        val formArray = ByteArray(file.available())
        file.read(formArray)
        file.close()
        return JSONArray(String(formArray))
    }
}