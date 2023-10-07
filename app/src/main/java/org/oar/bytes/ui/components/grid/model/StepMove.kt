package org.oar.bytes.ui.components.grid.model

import org.oar.bytes.model.Position

data class StepMove(
    val positionTile: Position,
    val positionDest: Position
): StepAction()