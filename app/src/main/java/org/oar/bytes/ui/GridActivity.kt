package org.oar.bytes.ui

import android.os.Bundle
import android.view.Window
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import org.oar.bytes.R
import org.oar.bytes.features.notification.NotificationService.clearScheduledNotifications
import org.oar.bytes.features.time.TimeController
import org.oar.bytes.ui.common.InitialLoad
import org.oar.bytes.ui.common.components.levelpanel.LevelPanelView
import org.oar.bytes.ui.common.pager.FragmentPager
import org.oar.bytes.ui.fragments.MainView
import org.oar.bytes.utils.SaveStateExt.hasState
import org.oar.bytes.utils.SaveStateExt.loadState
import org.oar.bytes.utils.SaveStateExt.saveState

class GridActivity : AppCompatActivity() {

    val levelPanel: LevelPanelView by lazy { findViewById(R.id.levelPanel) }
    private val pager: FragmentPager by lazy { findViewById(R.id.pager) }

    companion object {
        private const val MAIN_VIEW_INDEX = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        InitialLoad.loadData(this)
        setContentView(R.layout.activity_grid)

        pager.setCurrentItem(MAIN_VIEW_INDEX, false)
        runOnMainViews {
            switchView = this@GridActivity::switchView
            onCreate()
        }

        clearScheduledNotifications()

        if (hasState()) {
            loadState()
                ?.also { reloadState(it) }
                ?: runOnMainViews { newGame() }
        } else {
            runOnMainViews { newGame() }
        }

        onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (pager.currentItem == MAIN_VIEW_INDEX) {
                        pager.getActiveView<MainView>()
                            ?.onBack()
                            ?.takeIf { !it }
                            ?: finish()
                    } else {
                        pager.currentItem = MAIN_VIEW_INDEX
                    }
                }
            }
        )
    }

    override fun onPause() {
        TimeController.setLastShutdownTime()

        val json = JSONObject()
        TimeController.appendToJson(json)
        levelPanel.appendToJson(json)

        runOnMainViews {
            onPause()
            appendToJson(json)
            scheduleNotifications()
        }
        pager.getActiveView<MainView>()
            ?.onBlur()

        saveState(json)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        TimeController.notifyOfflineTime(this)

        runOnMainViews { onResume() }
        pager.getActiveView<MainView>()
            ?.onFocus()

        clearScheduledNotifications()
    }

    private fun switchView(item: Int) {
        pager.currentItem = item
    }

    private fun reloadState(json: JSONObject) {
        TimeController.fromJson(json)
        runOnMainViews { fromJson(json) }
        levelPanel.fromJson(json)
    }

    private fun runOnMainViews(function: MainView.() -> Unit) {
        pager.getViews()
            .map { it as MainView }
            .forEach { function(it) }
    }
}