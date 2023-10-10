package org.oar.bytes.ui.common.components.grid.model

import org.json.JSONObject
import org.oar.bytes.model.Position

data class StepUnmerge(
    val positionTile: Position,
    val positionDest: Position
): StepAction() {

    override val comparingField = positionTile

    override fun toJson() = JSONObject().apply {
        put("type", "unmerge")
    }
}