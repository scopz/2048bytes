package org.oar.bytes.ui.common.components.grid.model

import org.json.JSONObject
import org.oar.bytes.model.Position

data class StepMove(
    val positionTile: Position,
    val positionDest: Position
): StepAction() {
    companion object {
        fun fromJson(json: JSONObject): StepMove? {
            if (json.getString("type") != "move")
                return null

            val tile = Position(json.getInt("tileX"), json.getInt("tileY"))
            val dest = Position(json.getInt("destX"), json.getInt("destY"))
            return StepMove(tile, dest)
        }
    }

    override val comparingField = positionTile

    override fun toJson() = JSONObject().apply {
        put("type", "move")
        put("tileX", positionTile.x)
        put("tileY", positionTile.y)
        put("destX", positionDest.x)
        put("destY", positionDest.y)
    }
}