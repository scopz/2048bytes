package org.oar.bytes.ui.common.pager

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout

open class PageLayout(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {
    var focused = false
        set(value) {
            val hasChanged = field != value
            if (hasChanged) {
                field = value
                triggerOnFocusChange(value)
            }
        }

    var onFocusChange: ((Boolean) -> Unit)? = null

    open fun onBlur() { }
    open fun onFocus() { }

    private fun triggerOnFocusChange(value: Boolean) {
        if (value) onFocus() else onBlur()
        onFocusChange?.let { it(value) }

        getChildPagers()
            .mapNotNull { it.getActiveView() }
            .forEach { it.focused = value }
    }

    private fun ViewGroup.getChildPagers(): Sequence<FragmentPager> =
        (0 until childCount)
            .asSequence()
            .map { getChildAt(it) }
            .flatMap {
                when(it) {
                    is FragmentPager -> sequenceOf(it)
                    is ViewGroup -> it.getChildPagers()
                    else -> emptySequence()
                }
            }
}
