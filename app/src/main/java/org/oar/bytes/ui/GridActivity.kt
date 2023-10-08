package org.oar.bytes.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import org.oar.bytes.R
import org.oar.bytes.ui.common.InitialLoad
import org.oar.bytes.ui.common.components.grid.Grid2048View
import org.oar.bytes.utils.NumbersExt.sByte
import org.oar.bytes.utils.SaveStateUtils.hasState
import org.oar.bytes.utils.SaveStateUtils.loadState
import org.oar.bytes.utils.SaveStateUtils.saveState

class GridActivity : AppCompatActivity() {

    private val grid by lazy { findViewById<Grid2048View>(R.id.grid) }
    private val stored by lazy { findViewById<TextView>(R.id.stored) }

    private var storedValue = 0.sByte

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InitialLoad.loadData(this)
        setContentView(R.layout.activity_grid)

        stored.text = storedValue.toString()

        grid.setOnProduceByteListener {
            storedValue.add(it)
            stored.text = storedValue.toString()
        }

        grid.setOnGameOverListener {
            storedValue.substract(storedValue)
            stored.text = storedValue.toString()
            grid.restart()
        }

        grid.setOnReadyListener {
            if (hasState()) {
                loadState()
                    ?.also { reloadState(it) }
                    ?: grid.restart()
            } else {
                grid.restart()
            }
        }

        stored.setOnClickListener {
            grid.advanceLevel()
        }
    }

    override fun onPause() {
        val json = grid.toJson()
        json.put("storedValue", storedValue.value.toString())

        saveState(json)
        super.onPause()
    }

    private fun reloadState(json: JSONObject) {
        storedValue = json.getString("storedValue").sByte
        stored.text = storedValue.toString()

        grid.fromJson(json)
    }
}