package org.oar.bytes.ui.common.components.grid.services

import android.content.Context
import android.view.MotionEvent

class GridTouchControl(
    val context: Context
) {
    enum class Action {
        MOVE_LEFT, MOVE_RIGHT, MOVE_UP, MOVE_DOWN
    }

    private var dragPosX = 0f
    private var dragPosY = 0f
    private var actionDone = false

    fun onTouchEvent(event: MotionEvent): Action? {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                dragPosX = event.x
                dragPosY = event.y
                actionDone = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (!actionDone) {
                    val dx = event.x - dragPosX
                    val dy = event.y - dragPosY

                    return when {
                        dx > 35 -> Action.MOVE_RIGHT
                        dx < -35 -> Action.MOVE_LEFT
                        dy > 35 -> Action.MOVE_DOWN
                        dy < -35 -> Action.MOVE_UP
                        else -> null
                    }
                        ?.apply { actionDone = true }
                }
            }
        }
        return null
    }
}