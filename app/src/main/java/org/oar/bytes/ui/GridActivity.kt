package org.oar.bytes.ui

import android.content.Intent
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import org.oar.bytes.R
import org.oar.bytes.ui.common.InitialLoad
import org.oar.bytes.ui.common.components.HintsView
import org.oar.bytes.ui.common.components.grid.Grid2048View
import org.oar.bytes.ui.common.components.levelpanel.LevelPanelView
import org.oar.bytes.utils.Data
import org.oar.bytes.utils.NumbersExt.sByte
import org.oar.bytes.utils.SaveStateUtils.deleteState
import org.oar.bytes.utils.SaveStateUtils.hasState
import org.oar.bytes.utils.SaveStateUtils.loadState
import org.oar.bytes.utils.SaveStateUtils.saveState

class GridActivity : AppCompatActivity() {

    private val levelPanel by lazy { findViewById<LevelPanelView>(R.id.levelPanel) }
    private val hintsPanel by lazy { findViewById<HintsView>(R.id.hintsPanel) }
    private val grid by lazy { findViewById<Grid2048View>(R.id.grid) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        InitialLoad.loadData(this)
        setContentView(R.layout.activity_grid)

        if (intent.getBooleanExtra("reset", false)) {
            deleteState()
            Data.gridLevel = 1
        }

        grid.setOnProduceByteListener { _, value ->
            levelPanel.addBytes(value)
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

        levelPanel.setOnLongClickListener {
            val intent = Intent(this, GridActivity::class.java)
            intent.putExtra("reset", true)
            startActivity(intent)
            finish()
            true
        }

        levelPanel.setOnCapacityReachedListener {
            grid.paused = it
        }

        hintsPanel.setOnRevertClickListener {
            grid.revertLast()
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