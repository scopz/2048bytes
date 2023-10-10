package org.oar.bytes.ui.common.components.grid.model

import org.json.JSONObject
import org.oar.bytes.model.Position

abstract class StepAction {
    companion object {
        fun fromJson(json: JSONObject) =
            StepMove.fromJson(json) ?:
            StepMerge.fromJson(json) ?:
            throw Exception("Cannot unparse step: $json")
    }

    abstract val comparingField: Comparable<Position>

    abstract fun toJson(): JSONObject
}