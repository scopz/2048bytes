package org.oar.bytes.ui

import android.Manifest.permission.POST_NOTIFICATIONS
import android.R
import android.app.ActivityOptions.makeCustomAnimation
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.checkSelfPermission
import org.json.JSONArray
import org.oar.bytes.features.notification.NotificationService
import org.oar.bytes.model.Device
import org.oar.bytes.model.EnergyDevice
import org.oar.bytes.utils.ComponentsExt.getService
import org.oar.bytes.utils.Data
import org.oar.bytes.utils.JsonExt.mapJsonObject
import org.oar.bytes.utils.NumbersExt.sByte

class InitActivity : AppCompatActivity() {

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 66) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED)
                afterNotificationPermission()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        loadAssets()

        if (checkSelfPermission(this, POST_NOTIFICATIONS) == PERMISSION_GRANTED) {
            afterNotificationPermission()
        } else {
            requestPermissions(arrayOf(POST_NOTIFICATIONS), 66)
            NotificationService.configureChannels(this)
        }
    }

    private fun afterNotificationPermission() {

        if (!getService<AlarmManager>(ALARM_SERVICE).canScheduleExactAlarms()) {
            HandlerThread("alarm_service_intent_thread").apply {
                start()
                Handler(looper).post {
                    startActivity(
                        Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                            .apply { data = Uri.parse("package:$packageName") }
                    )
                }
                quitSafely()
            }
        }

        val intent = Intent(this, GridActivity::class.java)
        val options = makeCustomAnimation(this, R.anim.fade_in, R.anim.fade_out)
        startActivity(intent, options.toBundle())
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