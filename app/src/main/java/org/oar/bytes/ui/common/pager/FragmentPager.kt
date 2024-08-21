package org.oar.bytes.ui.common.pager

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import org.oar.bytes.utils.ComponentsExt.calculatedHeight

@Suppress("UNCHECKED_CAST")
class FragmentPager(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val activity = context as FragmentActivity
    private val pager: ViewPager2 = ViewPager2(context, attrs)
    private val fragments = mutableListOf<IdFragment<PageLayout>>()
    var onHeightChange: ((Int, Boolean) -> Unit)? = null

    init {
        pager.apply {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            pager.adapter = SectionsPagerAdapter()
            pager.isUserInputEnabled = false
        }

        addView(pager)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        List(childCount - 1) { 1 }
            .map { getChildAt(it).apply { removeView(this) } }
            .map { it as PageLayout }
            .map { IdFragment(it) }
            .also { fragments.addAll(it) }
    }

    fun setFragments(@LayoutRes vararg ids: Int) =
        ids
            .map { IdFragment<PageLayout>(it, activity.layoutInflater) }
            .also { fragments.addAll(it) }

    fun setCurrentItem(item: Int, smoothScroll: Boolean = true) {
        if (currentItem == item) return

        getActiveView<PageLayout>()?.focused = false
        pager.setCurrentItem(item, smoothScroll)
        getView<PageLayout>(item)?.focused = true

        onHeightChange?.also { listener ->
            getActiveView<PageLayout>()
                ?.calculatedHeight
                ?.also { listener(it, smoothScroll) }
        }
    }

    var currentItem: Int
        get() = pager.currentItem
        set(value) = setCurrentItem(value)

    fun getViews() = fragments.mapNotNull { it.viewInstance }

    fun <T: PageLayout> getView(position: Int) =
        if (position < 0 || pager.currentItem < fragments.size)
            fragments[position].viewInstance as? T
        else
            null
    fun <T: PageLayout> getActiveView(): T? = getView(pager.currentItem)

    inner class SectionsPagerAdapter : FragmentStateAdapter(activity.supportFragmentManager, activity.lifecycle) {
        override fun getItemCount() = fragments.size
        override fun createFragment(position: Int): Fragment = fragments[position]
    }

    class IdFragment<T : PageLayout>(
        @LayoutRes id: Int,
        inflater: LayoutInflater?,
        sourceView: T? = null
    ): Fragment() {
        constructor() : this(0, null)
        constructor(sourceView: T) : this(0, null, sourceView)

        val viewInstance = sourceView ?:
            if (id == 0) null else inflater?.inflate(id, null) as T?

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = viewInstance
    }
}

