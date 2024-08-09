package org.oar.bytes.ui.common.components.idlepanel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import org.oar.bytes.R
import org.oar.bytes.ui.SettingsActivity
import org.oar.bytes.utils.ComponentsExt.overridePendingSideTransition

class NavPanelView(
    context: Context,
    attr: AttributeSet? = null
) : FrameLayout(context, attr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.component_nav_panel, this, true)

        findViewById<View>(R.id.settingsButton).apply {
            visibility = View.VISIBLE
            setOnClickListener {
                context as Activity

                val intent = Intent(context, SettingsActivity::class.java)
                context.startActivity(intent)
                context.overridePendingSideTransition()
            }
        }
        findViewById<View>(R.id.prevButton).setOnClickListener {  }
        findViewById<View>(R.id.nextButton).setOnClickListener {  }
    }
}