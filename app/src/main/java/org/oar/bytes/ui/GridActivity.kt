package org.oar.bytes.ui

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import org.json.JSONObject
import org.oar.bytes.R
import org.oar.bytes.features.time.TimeController
import org.oar.bytes.ui.common.InitialLoad
import org.oar.bytes.ui.common.components.devices.SpeedDeviceView
import org.oar.bytes.ui.common.components.devices.TimeEnergyView
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
    private val idlePanel by lazy { findViewById<IdlePanelView>(R.id.idle) }
    private val pager by lazy { findViewById<ViewPager2>(R.id.pager) }

    private lateinit var hintsPanel: HintsView
    private lateinit var grid: Grid2048View

    private lateinit var speedView: SpeedDeviceView
    private lateinit var timeView: TimeEnergyView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        InitialLoad.loadData(this)
        setContentView(R.layout.activity_grid)

        if (intent.getBooleanExtra("reset", false)) {
            deleteState()
            Data.gridLevel = 1
        }

        val adapter = SectionsPagerAdapter()
        pager.adapter = adapter
        pager.isUserInputEnabled = false
        pager.setCurrentItem(1, false)

        adapter.gridFragment.viewInstance.apply {
            grid = findViewById(R.id.grid)
            hintsPanel = findViewById(R.id.hintsPanel)

            addOnLayoutChangeListener(object: View.OnLayoutChangeListener {
                override fun onLayoutChange(
                    v: View,
                    left: Int, top: Int, right: Int, bottom: Int,
                    oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
                ) {
                    pager.layoutParams.height = bottom - top
                    removeOnLayoutChangeListener(this)
                }
            })

        }

        adapter.energyFragment.viewInstance.apply {
            timeView = findViewById(R.id.energyDeviceList)
            timeView.setOnEnergyChangedListener {
                idlePanel.maxTime = it
            }
        }
        adapter.deviceFragment.viewInstance.apply {
            speedView = findViewById(R.id.deviceList)
            speedView.setOnSpeedChangedListener {
                idlePanel.bytesSec = it
            }
        }

        idlePanel.setOnClickTimeListener {
            pager.currentItem = if (pager.currentItem == 0) 1 else 0
        }

        idlePanel.setOnClickSpeedListener {
            pager.currentItem = if (pager.currentItem == 2) 1 else 2
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

        hintsPanel.setOnSwapClickListener { on ->
            delayedHintAction(hintsPanel.swap, grid::swapTilesHint, on)
        }

        hintsPanel.setOnAddClickListener { on ->
            delayedHintAction(hintsPanel.add, grid::addTileHint, on)
        }

        hintsPanel.setOnRevertClickListener {
            if (grid.revertLastHint()) {
                hintsPanel.revert.setProgress(0)
            }
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
        timeView.appendToJson(json)
        speedView.appendToJson(json)

        json.put("gridLevel", Data.gridLevel)

        saveState(json)
        super.onPause()
    }

    private fun reloadState(json: JSONObject) {
        Data.gridLevel = json.getInt("gridLevel")
        speedView.fromJson(json)
        timeView.fromJson(json)
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


    inner class SectionsPagerAdapter : FragmentStateAdapter(supportFragmentManager, lifecycle) {
        val gridFragment = Id2Fragment<View>(R.layout.fragment_grid, layoutInflater)
        val energyFragment = Id2Fragment<View>(R.layout.fragment_time_energy, layoutInflater)
        val deviceFragment = Id2Fragment<View>(R.layout.fragment_speed_device, layoutInflater)

        override fun getItemCount() = 3

        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> energyFragment
                2 -> deviceFragment
                else -> gridFragment
            }
        }
    }

    class Id2Fragment<T : View>(@LayoutRes id: Int, inflater: LayoutInflater): Fragment() {
        @Suppress("UNCHECKED_CAST")
        val viewInstance: T = inflater.inflate(id, null) as T

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = viewInstance
    }
}