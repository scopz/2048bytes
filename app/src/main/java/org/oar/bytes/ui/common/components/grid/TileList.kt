package org.oar.bytes.ui.common.components.grid

import org.oar.bytes.model.Position

class TileList<T : GridTile> : MutableList<T> by mutableListOf() {

    fun findByPosition(pos: Position) = find { it.pos == pos }
}