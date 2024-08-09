package org.oar.bytes.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import org.json.JSONObject
import org.oar.bytes.R
import org.oar.bytes.features.animate.AnimationChain
import org.oar.bytes.features.animate.Animator
import org.oar.bytes.features.notification.NotificationChannel
import org.oar.bytes.features.notification.NotificationService.clearScheduledNotifications
import org.oar.bytes.features.notification.NotificationService.scheduleNotification
import org.oar.bytes.features.time.TimeController
import org.oar.bytes.ui.animations.HintsProgressAnimation
import org.oar.bytes.ui.animations.LevelProgressAnimation
import org.oar.bytes.ui.animations.WaitAnimation
import org.oar.bytes.ui.common.ConfirmDialog
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

        clearScheduledNotifications()

        val adapter = SectionsPagerAdapter()
        pager.adapter = adapter
        pager.isUserInputEnabled = false
        pager.setCurrentItem(1, false)

        adapter.gridFragment.viewInstance?.apply {
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

        adapter.energyFragment.viewInstance?.apply {
            timeView = findViewById(R.id.energyDeviceList)
            timeView.setOnEnergyChangedListener { energy, jsonLoad ->
                idlePanel.maxTime = energy
                if (!jsonLoad) {
                    idlePanel.notifyMaxTimeUpdated()
                }
            }
        }
        adapter.deviceFragment.viewInstance?.apply {
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

        idlePanel.setOnProduceByteListener { secs, bytes, animate ->
                if (animate) {
                    Animator.addAndStart(listOf(
                        AnimationChain(idlePanel)
                            .next { WaitAnimation(1000) }
                            .next { LevelProgressAnimation(levelPanel, bytes, 1000) },
                        AnimationChain(hintsPanel)
                            .next { WaitAnimation(1000) }
                            .next { HintsProgressAnimation(hintsPanel, secs, 1000) }
                    ))
                } else {
                    levelPanel.addBytes(bytes)
                    hintsPanel.addProgress(secs)
                }
        }
        grid.setOnProduceByteListener {
            levelPanel.addBytes(it.mergedValue)
            hintsPanel.addProgress(it.mergedLevels.sumOf { v -> v - 1 } * 12)
        }

        grid.setOnGameOverListener {
            //levelPanel.storedValue = 0.sByte
            //grid.restart()
        }

        grid.setOnLongClickListener {
            ConfirmDialog.show(this, R.string.title_confirm, R.string.restart_confirm, {
                Animator.stopAll()
                levelPanel.storedValue = 0.sByte
                grid.restart()
            })
            true
        }

        levelPanel.setLevelUpListener {
            grid.advancedGridLevel()
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

        hintsPanel.setOnImproveLowerClickListener {
            if (grid.improveLowerHint()) {
                hintsPanel.improveLower.setProgress(0)
            }
        }

        hintsPanel.setOnRevertClickListener {
            if (grid.revertLastHint()) {
                hintsPanel.revert.setProgress(0)
            }
        }

        hintsPanel.setOnSwapClickListener { on ->
            delayedHintAction(hintsPanel.swap, grid::swapTilesHint, on)
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
        speedView.appendToJson(json)
        timeView.appendToJson(json)

        json.put("gridLevel", Data.gridLevel)

        saveState(json)
        scheduleNotifications()

        super.onPause()
    }

    private fun scheduleNotifications() {
        if (idlePanel.currentTime > 0) {
            scheduleNotification(NotificationChannel.IDLE_ENDED, idlePanel.currentTime)
        }

        val bytesPerSecond = idlePanel.bytesSec
        if (!bytesPerSecond.isBiggerThanZero) return

        val pendingBytesToCap = levelPanel.capacity - levelPanel.storedValue
        if (pendingBytesToCap.isBiggerThanZero) {
            val secondsToCap = (pendingBytesToCap/bytesPerSecond).value.toInt()
            if (secondsToCap < idlePanel.currentTime) {
                scheduleNotification(NotificationChannel.MAX_CAPACITY_REACHED, secondsToCap)
            }
        }

        val pendingBytesToLevel = levelPanel.toLevel - levelPanel.storedValue
        if (pendingBytesToLevel <= pendingBytesToCap && pendingBytesToLevel.isBiggerThanZero) {
            val secondsToLevel = (pendingBytesToLevel/bytesPerSecond).value.toInt()
            if (secondsToLevel < idlePanel.currentTime) {
                scheduleNotification(NotificationChannel.LEVEL_AVAILABLE, secondsToLevel)
            }
        }
    }

    private fun reloadState(json: JSONObject) {
        Data.gridLevel = json.getInt("gridLevel")
        timeView.fromJson(json)
        speedView.fromJson(json)
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
        clearScheduledNotifications()
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

    class Id2Fragment<T : View>(
        @LayoutRes id: Int,
        inflater: LayoutInflater?
    ): Fragment() {
        constructor() : this(0, null)

        @Suppress("UNCHECKED_CAST")
        val viewInstance = if (id == 0) null else inflater?.inflate(id, null) as T?

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = viewInstance
    }
}