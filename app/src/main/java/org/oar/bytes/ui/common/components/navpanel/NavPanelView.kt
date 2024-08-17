package org.oar.bytes.ui.common.components.navpanel

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import org.oar.bytes.R
import org.oar.bytes.R.styleable.NavPanelView
import org.oar.bytes.R.styleable.NavPanelView_settingsButton

class NavPanelView(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    var onSettingsButtonClick: (() -> Unit)? = null
    var onNextButtonClick: (() -> Unit)? = null
    var onPrevButtonClick: (() -> Unit)? = null

    init {
        val styledAttributes = context.obtainStyledAttributes(attrs, NavPanelView)
        val showSettingsButton = styledAttributes.getBoolean(NavPanelView_settingsButton, false)
        styledAttributes.recycle()

        LayoutInflater.from(context).inflate(R.layout.component_nav_panel, this, true)

        findViewById<View>(R.id.settingsButton).apply {
            visibility = if (showSettingsButton) View.VISIBLE else View.GONE
            setOnClickListener {
                onSettingsButtonClick?.let { it() }
            }
        }

        findViewById<View>(R.id.prevButton).setOnClickListener {
            onPrevButtonClick?.let { it() }
        }
        findViewById<View>(R.id.nextButton).setOnClickListener {
            onNextButtonClick?.let { it() }
        }
    }

    fun hidePrevButton(){
        findViewById<View>(R.id.prevButton).visibility = GONE
        findViewById<View>(R.id.nextButton)
            .let { it.layoutParams as MarginLayoutParams }
            .marginStart = 0
    }

    fun hideNextButton(){
        findViewById<View>(R.id.nextButton).visibility = GONE
        findViewById<View>(R.id.prevButton)
            .let { it.layoutParams as MarginLayoutParams }
            .marginEnd = 0
    }
}