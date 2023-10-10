package org.oar.bytes.ui.common.components.grid.model

import org.json.JSONObject
import org.oar.bytes.model.Position

data class StepMerge(
    val positionBase: Position,
    val positionDest: Position,
): StepAction() {
    companion object {
        fun fromJson(json: JSONObject): StepMerge? {
            if (json.getString("type") != "merge")
                return null

            val base = Position(json.getInt("baseX"), json.getInt("baseY"))
            val dest = Position(json.getInt("destX"), json.getInt("destY"))
            return StepMerge(base, dest)
        }
    }

    override val comparingField = positionBase

    override fun toJson() = JSONObject().apply {
        put("type", "merge")
        put("baseX", positionBase.x)
        put("baseY", positionBase.y)
        put("destX", positionDest.x)
        put("destY", positionDest.y)
    }
}