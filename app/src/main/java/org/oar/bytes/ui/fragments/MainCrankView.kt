package org.oar.bytes.ui.fragments

import android.content.Context
import android.util.AttributeSet
import org.json.JSONObject
import org.oar.bytes.R
import org.oar.bytes.ui.common.components.crank.CrankView
import org.oar.bytes.ui.common.components.crankpower.CrankPowerView
import org.oar.bytes.ui.common.components.navpanel.NavPanelView
import org.oar.bytes.ui.fragments.MainView.MainDefinition.MAIN
import org.oar.bytes.utils.extensions.NumbersExt.sByte

class MainCrankView(
    context: Context,
    attrs: AttributeSet? = null
) : MainView(context, attrs, R.layout.fragment_general_crank) {

    private val crank by lazy { findViewById<CrankView>(R.id.crankView) }
    private val crankPower by lazy { findViewById<CrankPowerView>(R.id.crankPower) }

    override fun onCreate() {
        findViewById<NavPanelView>(R.id.nav).apply {
            onNextButtonClick = { switchView(MAIN) }
            hidePrevButton()
        }

        crank.onStatsChange = { _, speed, mMaxSpeed, bytes ->
            val bytesPerSecond = bytes.value.toBigDecimal() * speed.toBigDecimal()
            crankPower.setData("${bytesPerSecond.sByte}/s", speed / mMaxSpeed)
        }

        crank.onCapacityChange = {
            crankPower.setSubText(it.toString())
        }
    }

    override fun onBlur() {
        super.onBlur()
        crank.numb = true
        crankPower.numb = true
    }

    override fun onFocus() {
        super.onFocus()
        crank.numb = false
        crankPower.numb = false
    }

    override fun fromJson(json: JSONObject) {
        crank.fromJson(json)
    }

    override fun appendToJson(json: JSONObject) {
        crank.appendToJson(json)
    }
}