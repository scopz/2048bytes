package org.oar.bytes.ui.common.pager

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

class FragmentPager(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val activity = context as FragmentActivity
    private val pager: ViewPager2 = ViewPager2(context, attrs)
    private val fragments = mutableListOf<IdFragment<View>>()

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

    fun setFragments(@LayoutRes vararg ids: Int) =
        ids
            .map { IdFragment<View>(it, activity.layoutInflater) }
            .also { fragments.addAll(it) }

    fun setCurrentItem(item: Int, smoothScroll: Boolean = true) =
        pager.setCurrentItem(item, smoothScroll)

    var currentItem: Int
        get() = pager.currentItem
        set(value) = setCurrentItem(value)

    fun getView(position: Int) = fragments[position].viewInstance

    inner class SectionsPagerAdapter : FragmentStateAdapter(activity.supportFragmentManager, activity.lifecycle) {
        override fun getItemCount() = fragments.size
        override fun createFragment(position: Int): Fragment = fragments[position]
    }

    class IdFragment<T : View>(
        @LayoutRes id: Int,
        inflater: LayoutInflater?
    ): Fragment() {
        constructor() : this(0, null)

        @Suppress("UNCHECKED_CAST")
        val viewInstance = if (id == 0) null else inflater?.inflate(id, null) as T?

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = viewInstance
    }
}