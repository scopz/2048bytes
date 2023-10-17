package org.oar.bytes.ui.common.components.grid.services

import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class GridTouchControlService(
    val parent: View
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
                    val ax = abs(dx)
                    val ay = abs(dy)

                    return when {
                        dx > 45 && ax > ay -> Action.MOVE_RIGHT
                        dx < -45 && ax > ay -> Action.MOVE_LEFT
                        dy > 45 -> Action.MOVE_DOWN
                        dy < -45 -> Action.MOVE_UP
                        else -> null
                    }
                        ?.apply { actionDone = true }
                }
            }
        }
        return null
    }
}