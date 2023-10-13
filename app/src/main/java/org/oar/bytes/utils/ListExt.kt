package org.oar.bytes.utils

import org.oar.bytes.model.Position
import org.oar.bytes.ui.common.components.grid.GridTile

object ListExt {
    fun <T : GridTile> List<T>.findByPosition(pos: Position) = find { it.pos == pos }
}