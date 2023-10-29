package org.oar.bytes.ui

import android.content.Intent
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import org.json.JSONObject
import org.oar.bytes.R
import org.oar.bytes.features.time.TimeController
import org.oar.bytes.ui.common.InitialLoad
import org.oar.bytes.ui.common.components.grid.Grid2048View
import org.oar.bytes.ui.common.components.grid.GridTile
import org.oar.bytes.ui.common.components.hints.HintButtonView
import org.oar.bytes.ui.common.components.hints.HintsView
import org.oar.bytes.ui.common.components.idlepanel.IdlePanelView
import org.oar.bytes.ui.common.components.levelpanel.LevelPanelView
import org.oar.bytes.utils.Data
import org.oar.bytes.utils.NumbersExt.sByte
import org.oar.bytes.utils.SaveStateExt.deleteState
import org.oar.bytes.utils.SaveStateExt.hasState
import org.oar.bytes.utils.SaveStateExt.loadState
import org.oar.bytes.utils.SaveStateExt.saveState

class GridActivity : AppCompatActivity() {

    private val levelPanel by lazy { findViewById<LevelPanelView>(R.id.levelPanel) }
    private val hintsPanel by lazy { findViewById<HintsView>(R.id.hintsPanel) }
    private val grid by lazy { findViewById<Grid2048View>(R.id.grid) }
    private val idlePanel by lazy { findViewById<IdlePanelView>(R.id.idle) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        InitialLoad.loadData(this)
        setContentView(R.layout.activity_grid)

        if (intent.getBooleanExtra("reset", false)) {
            deleteState()
            Data.gridLevel = 1
        }

        idlePanel.setOnProduceByteListener { secs, bytes ->
            levelPanel.addBytes(bytes)
            hintsPanel.addProgress(secs)
        }
        grid.setOnProduceByteListener { count, _, value ->
            levelPanel.addBytes(value)
            hintsPanel.addProgress(count * 10)
        }

        grid.setOnGameOverListener {
            levelPanel.storedValue = 0.sByte
            grid.restart()
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

        // HINTS

        var gridObserver: MutableLiveData<GridTile>? = null
        fun cleanObserver() {
            gridObserver?.apply {
                removeObservers(this@GridActivity)
                gridObserver = null
            }
        }
        fun delayedHintAction(button: HintButtonView, action: () -> MutableLiveData<GridTile>?, on: Boolean) {
            button.active = on
            if (on) {
                gridObserver = action()
                gridObserver?.observe(this) {
                    button.setProgress(0)
                    button.active = false
                    cleanObserver()
                }
            } else {
                grid.clearSelectAction()
                cleanObserver()
            }
        }

        hintsPanel.setOnAddClickListener { on ->
            delayedHintAction(hintsPanel.add, grid::addTileHint, on)
        }

        hintsPanel.setOnRevertClickListener {
            grid.revertLastHint()
        }

        hintsPanel.setOnRemoveClickListener { on ->
            delayedHintAction(hintsPanel.remove, grid::removeTileHint, on)
        }

        if (hasState()) {
            loadState()
                ?.also { reloadState(it) }
                ?: grid.restart()
        } else {
            grid.restart()
        }
    }

    override fun onPause() {
        TimeController.setLastShutdownTime()
        idlePanel.stopTimer()

        val json = JSONObject()
        TimeController.appendToJson(json)
        grid.appendToJson(json)
        levelPanel.appendToJson(json)
        hintsPanel.appendToJson(json)
        idlePanel.appendToJson(json)

        json.put("gridLevel", Data.gridLevel)

        saveState(json)
        super.onPause()
    }

    private fun reloadState(json: JSONObject) {
        Data.gridLevel = json.getInt("gridLevel")
        idlePanel.fromJson(json)
        levelPanel.fromJson(json)
        hintsPanel.fromJson(json)
        grid.fromJson(json)
        TimeController.fromJson(json)
    }

    override fun onResume() {
        super.onResume()
        TimeController.notifyOfflineTime(this)
        idlePanel.startTimer()
    }
}