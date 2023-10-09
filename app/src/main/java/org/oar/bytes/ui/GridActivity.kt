package org.oar.bytes.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import org.oar.bytes.R
import org.oar.bytes.ui.common.InitialLoad
import org.oar.bytes.ui.common.components.LevelPanelGrid
import org.oar.bytes.ui.common.components.grid.Grid2048View
import org.oar.bytes.utils.Data
import org.oar.bytes.utils.NumbersExt.sByte
import org.oar.bytes.utils.SaveStateUtils.hasState
import org.oar.bytes.utils.SaveStateUtils.loadState
import org.oar.bytes.utils.SaveStateUtils.saveState

class GridActivity : AppCompatActivity() {

    private val grid by lazy { findViewById<Grid2048View>(R.id.grid) }
    private val levelPanel by lazy { findViewById<LevelPanelGrid>(R.id.levelPanel) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InitialLoad.loadData(this)
        setContentView(R.layout.activity_grid)


        grid.setOnProduceByteListener {
            levelPanel.addBytes(it)
        }

        grid.setOnGameOverListener {
            levelPanel.storedValue = 0.sByte
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

        levelPanel.setLevelUpListener {
            grid.advancedGridLevel()
        }
    }

    override fun onPause() {
        val json = grid.toJson()
        levelPanel.appendToJson(json)
        json.put("gridLevel", Data.gridLevel)

        saveState(json)
        super.onPause()
    }

    private fun reloadState(json: JSONObject) {
        Data.gridLevel = json.getInt("gridLevel")
        levelPanel.fromJson(json)
        grid.fromJson(json)
    }
}