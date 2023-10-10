package org.oar.bytes.ui.common.components.grid

import org.oar.bytes.model.Position

class TileList<T : GridTile>(
    list: List<T> = listOf()
) : MutableList<T> by list.toMutableList() {

    fun findByPosition(pos: Position) = find { it.pos == pos }
}