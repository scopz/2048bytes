package org.oar.bytes.ui.common.components.grid.model

import org.oar.bytes.model.Position

data class StepMerge(
    val positionBase: Position,
    val positionDest: Position,
): StepAction()