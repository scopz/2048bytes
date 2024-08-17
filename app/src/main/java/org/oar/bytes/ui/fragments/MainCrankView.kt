package org.oar.bytes.ui.fragments

import android.content.Context
import android.util.AttributeSet
import org.oar.bytes.R
import org.oar.bytes.ui.common.components.navpanel.NavPanelView
import org.oar.bytes.ui.fragments.MainView.MainDefinition.MAIN

class MainCrankView(
    context: Context,
    attrs: AttributeSet? = null
) : MainView(context, attrs, R.layout.fragment_general_crank) {

    override fun onCreate() {
        findViewById<NavPanelView>(R.id.nav).apply {
            onNextButtonClick = { switchView(MAIN) }
            hidePrevButton()
        }
    }
}