package org.oar.bytes.ui.common.components.grid.model

import org.oar.bytes.model.Position

data class StepMove(
    val positionTile: Position,
    val positionDest: Position
): StepAction()