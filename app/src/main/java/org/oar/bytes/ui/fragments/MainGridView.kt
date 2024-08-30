package org.oar.bytes.ui.fragments

import android.content.Context
import android.util.AttributeSet
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import org.json.JSONObject
import org.oar.bytes.R
import org.oar.bytes.features.animate.AnimationChain
import org.oar.bytes.features.animate.Animator
import org.oar.bytes.features.notification.NotificationChannel
import org.oar.bytes.features.notification.NotificationService.scheduleNotification
import org.oar.bytes.ui.animations.HintsProgressAnimation
import org.oar.bytes.ui.animations.LevelProgressAnimation
import org.oar.bytes.ui.animations.WaitAnimation
import org.oar.bytes.ui.common.ConfirmDialog
import org.oar.bytes.ui.common.components.devices.SpeedDeviceView
import org.oar.bytes.ui.common.components.devices.TimeEnergyView
import org.oar.bytes.ui.common.components.grid.Grid2048View
import org.oar.bytes.ui.common.components.grid.GridTile
import org.oar.bytes.ui.common.components.hints.HintButtonView
import org.oar.bytes.ui.common.components.hints.HintsView
import org.oar.bytes.ui.common.components.idlepanel.IdlePanelView
import org.oar.bytes.ui.common.components.navpanel.NavPanelView
import org.oar.bytes.ui.common.pager.FragmentPager
import org.oar.bytes.ui.common.pager.PageLayout
import org.oar.bytes.ui.fragments.MainView.MainDefinition.CRANK
import org.oar.bytes.ui.fragments.MainView.MainDefinition.SETTINGS
import org.oar.bytes.utils.Data
import org.oar.bytes.utils.extensions.ComponentsExt.calculatedHeight
import org.oar.bytes.utils.extensions.NumbersExt.sByte

class MainGridView(
    context: Context,
    attrs: AttributeSet? = null
) : MainView(context, attrs, R.layout.fragment_general_grid) {

    private val levelPanel by lazy { activity.levelPanel }
    private val idlePanel by lazy { findViewById<IdlePanelView>(R.id.idle) }
    private val pager by lazy { findViewById<FragmentPager>(R.id.pager) }

    private lateinit var hintsPanel: HintsView
    private lateinit var grid: Grid2048View

    private lateinit var speedView: SpeedDeviceView
    private lateinit var timeView: TimeEnergyView

    override fun onCreate() {
        pager.setFragments(
            R.layout.fragment_time_energy,
            R.layout.fragment_grid,
            R.layout.fragment_speed_device
        )

        findViewById<NavPanelView>(R.id.nav).apply {
            onSettingsButtonClick = { switchView(SETTINGS) }
//            onNextButtonClick = { activity.changeHeight(animate = true) }
            hideNextButton()
            onPrevButtonClick = { switchView(CRANK) }
        }

        pager.getView<PageLayout>(1)?.apply {
            grid = findViewById(R.id.grid)
            hintsPanel = findViewById(R.id.hintsPanel)

            onFocusChange = { hintsPanel.numb = !(it && focused) }

            listenNextLayoutChange { _, top, _, bottom ->
                this@MainGridView.apply {
                    listenNextLayoutChange { _, _, _, _ ->
                        activity.changeHeight(calculatedHeight, true, true)
                    }
                }
                pager.layoutParams.height = bottom - top
            }
        }

        pager.getView<PageLayout>(0)?.apply {
            timeView = findViewById(R.id.energyDeviceList)
            timeView.setOnEnergyChangedListener { energy, jsonLoad ->
                idlePanel.maxTime = energy
                if (!jsonLoad) {
                    idlePanel.notifyMaxTimeUpdated()
                }
            }
        }
        pager.getView<PageLayout>(2)?.apply {
            speedView = findViewById(R.id.deviceList)
            speedView.setOnSpeedChangedListener {
                idlePanel.bytesSec = it
            }
        }
        pager.setCurrentItem(1, false)

        idlePanel.setOnClickTimeListener {
            pager.currentItem = if (pager.currentItem == 0) 1 else 0
        }

        idlePanel.setOnClickSpeedListener {
            pager.currentItem = if (pager.currentItem == 2) 1 else 2
        }

        idlePanel.onProduceByteListener = { secs, bytes, animate ->
            if (animate) {
                Data.bytes.apply {
                    finalValue = value + bytes
                }
                hintsPanel.setFinalSeconds(secs)

                Animator.addAndStart(listOf(
                    AnimationChain(idlePanel)
                        .next { WaitAnimation(700) }
                        .next { LevelProgressAnimation(levelPanel, bytes, 1000) },
                    AnimationChain(hintsPanel)
                        .next { WaitAnimation(700) }
                        .next { HintsProgressAnimation(hintsPanel, secs, 1000) }
                ))
            } else {
                if (bytes.isBiggerThanZero) {
                    Data.bytes.operate { it + bytes }
                }
                hintsPanel.addSeconds(secs)
            }
        }
        grid.setOnProduceByteListener { values ->
            Data.bytes.operate { it + values.mergedValue }
            val secondsToAdd = values.mergedLevels.sumOf { v -> v - 1 } * 12
            hintsPanel.addSeconds(secondsToAdd)
        }

        grid.setOnGameOverListener {
            //levelPanel.storedValue = 0.sByte
            //grid.restart()
        }

        grid.setOnLongClickListener {
            ConfirmDialog.show(context, R.string.title_confirm, R.string.restart_confirm, {
                Animator.stopAll()
                Data.bytes.value = 0.sByte
                grid.restart()
            })
            true
        }

        // TODO: Move to GridActivity somehow
        Data.gameLevel.observe(context as LifecycleOwner) {
            grid.advancedGridLevel()
        }

        levelPanel.onCapacityReachedListener = {
            grid.paused = it
        }

        // HINTS
        var gridObserver: MutableLiveData<GridTile>? = null
        fun cleanObserver() {
            gridObserver?.apply {
                removeObservers(activity)
                gridObserver = null
            }
        }

        fun delayedHintAction(
            button: HintButtonView,
            action: () -> MutableLiveData<GridTile>?,
            on: Boolean
        ) {
            button.active = on
            if (on) {
                gridObserver = action()
                gridObserver?.observe(activity) {
                    button.reset()
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
                hintsPanel.improveLower.reset()
            }
        }

        hintsPanel.setOnRevertClickListener {
            if (grid.revertLastHint()) {
                hintsPanel.revert.reset()
            }
        }

        hintsPanel.setOnSwapClickListener { on ->
            delayedHintAction(hintsPanel.swap, grid::swapTilesHint, on)
        }

        hintsPanel.setOnRemoveClickListener { on ->
            delayedHintAction(hintsPanel.remove, grid::removeTileHint, on)
        }
    }

    override fun onFocus() {
        super.onFocus()
        pager.setCurrentItem(1, false)
    }

    override fun onBack(): Boolean {
        if (pager.currentItem != 1) {
            pager.currentItem = 1;
            return false
        }
        return true
    }

    override fun newGame() {
        grid.restart()
    }

    override fun onPause() {
        idlePanel.stopTimer()
    }

    override fun onResume() {
        idlePanel.startTimer()
    }

    override fun fromJson(json: JSONObject) {
        timeView.fromJson(json)
        speedView.fromJson(json)
        idlePanel.fromJson(json)
        hintsPanel.fromJson(json)
        grid.fromJson(json)
    }

    override fun appendToJson(json: JSONObject) {
        grid.appendToJson(json)
        hintsPanel.appendToJson(json)
        idlePanel.appendToJson(json)
        speedView.appendToJson(json)
        timeView.appendToJson(json)
    }

    override fun scheduleNotifications() {
        if (idlePanel.currentTime > 0) {
            context.scheduleNotification(NotificationChannel.IDLE_ENDED, idlePanel.currentTime)
        }

        val bytesPerSecond = idlePanel.bytesSec
        if (!bytesPerSecond.isBiggerThanZero) return

        val pendingBytesToCap = Data.capacity.value - Data.bytes.value
        if (pendingBytesToCap.isBiggerThanZero) {
            val secondsToCap = (pendingBytesToCap/bytesPerSecond).value.toInt()
            if (secondsToCap < idlePanel.currentTime) {
                context.scheduleNotification(NotificationChannel.MAX_CAPACITY_REACHED, secondsToCap)
            }
        }

        val pendingBytesToLevel = levelPanel.expRequired - Data.bytes.value
        if (pendingBytesToLevel <= pendingBytesToCap && pendingBytesToLevel.isBiggerThanZero) {
            val secondsToLevel = (pendingBytesToLevel/bytesPerSecond).value.toInt()
            if (secondsToLevel < idlePanel.currentTime) {
                context.scheduleNotification(NotificationChannel.LEVEL_AVAILABLE, secondsToLevel)
            }
        }
    }
}