package org.oar.bytes.ui.components.grid.services

import android.content.Context
import org.oar.bytes.model.Position
import org.oar.bytes.model.SByte
import org.oar.bytes.ui.components.grid.GridTile
import org.oar.bytes.ui.components.grid.TileList
import org.oar.bytes.ui.components.grid.model.StepAction
import org.oar.bytes.ui.components.grid.model.StepMerge
import org.oar.bytes.ui.components.grid.model.StepMove

class GridStepsGenerator(
    val context: Context
) {

    fun applyStep(tiles: TileList<GridTile>, step: StepAction) {
        when(step) {
            is StepMove -> moveTile(tiles, step)
            is StepMerge -> mergeTile(tiles, step)
        }
    }

    private fun moveTile(tiles: TileList<GridTile>, step: StepMove) {
        val tile = tiles.findByPosition(step.positionTile)

        tiles.replaceAll {
            if (it == tile) GridTile(context, tile.value, step.positionDest, tile.level)
            else it
        }
    }

    private fun mergeTile(tiles: TileList<GridTile>, step: StepMerge) {
        val tileBase = tiles.findByPosition(step.positionBase)
        val tileDest = tiles.findByPosition(step.positionDest)

        tiles.removeIf { it == tileBase }
        tiles.forEach {
            if (it == tileDest) {
                it.value.doubleValue()
            }
        }
    }

    private fun internalMoveTile(tiles: TileList<MergeableGridTile>, step: StepMove) {
        val tile = tiles.findByPosition(step.positionTile)

        tiles.replaceAll {
            if (it == tile) MergeableGridTile(tile.value, step.positionDest)
            else it
        }
    }

    private fun internalMergeTile(tiles: TileList<MergeableGridTile>, step: StepMerge) {
        val tileBase = tiles.findByPosition(step.positionBase)
        val tileDest = tiles.findByPosition(step.positionDest)

        tiles.removeIf { it == tileBase }
        tiles.forEach {
            if (it == tileDest) {
                it.value.doubleValue()
                it.merged = true
            }
        }
    }

    private fun createWorkableList(tiles: TileList<GridTile>): TileList<MergeableGridTile> {
        val workableList = TileList<MergeableGridTile>()
        tiles.map { MergeableGridTile(it.value.clone(), it.pos) }
            .also { workableList.addAll(it) }
        return workableList
    }

    fun moveLeft(tiles: TileList<GridTile>) = internalMove(
        tiles,
        rowsProgression = (0 until 4),
        columnsProgression = (1 until 4),
        watchPositions = { it.leftPositions },
        prevPosition = { it.right },
        rootPosition = { x, y -> Position(0, y)}
    )

    fun moveRight(tiles: TileList<GridTile>) = internalMove(
        tiles,
        rowsProgression = (0 until 4),
        columnsProgression = (0 until 3).reversed(),
        watchPositions = { it.rightPositions },
        prevPosition = { it.left },
        rootPosition = { x, y -> Position(3, y)}
    )

    fun moveUp(tiles: TileList<GridTile>) = internalMove(
        tiles,
        rowsProgression = (1 until 4),
        columnsProgression = (0 until 4),
        watchPositions = { it.topPositions },
        prevPosition = { it.bottom },
        rootPosition = { x, y -> Position(x, 0)}
    )

    fun moveDown(tiles: TileList<GridTile>) = internalMove(
        tiles,
        rowsProgression = (0 until 3).reversed(),
        columnsProgression = (0 until 4),
        watchPositions = { it.botPositions },
        prevPosition = { it.top },
        rootPosition = { x, y -> Position(x, 3)}
    )

    private fun internalMove(
        tiles: TileList<GridTile>,
        rowsProgression: IntProgression,
        columnsProgression: IntProgression,
        watchPositions: (Position) -> List<Position>,
        prevPosition: (Position) -> Position?,
        rootPosition: (Int, Int) -> Position
    ): List<StepAction> {
        val steps = mutableListOf<StepAction>()
        val modifyingTiles = createWorkableList(tiles)

        fun move(tile: GridTile, pos: Position) {
            val step = StepMove(tile.pos, pos)
            steps.add(step)
            internalMoveTile(modifyingTiles, step)
        }

        fun merge(tileBase: GridTile, tileDest: GridTile) {
            val step = StepMerge(tileBase.pos, tileDest.pos)
            steps.add(step)
            internalMergeTile(modifyingTiles, step)
        }

        rowsProgression.forEach { row ->
            columnsProgression.forEach { column ->
                modifyingTiles.findByPosition(Position(column, row))?.also { tile ->
                    watchPositions(tile.pos)
                        .map { modifyingTiles.findByPosition(it) }
                        .firstOrNull { it != null }
                        ?.also {
                            if (it.value == tile.value && !it.merged) {
                                merge(tile, it)
                            } else {
                                val prevTilePosition = prevPosition(it.pos)
                                if (prevTilePosition != null && prevTilePosition != tile.pos) {
                                    move(tile, prevTilePosition)
                                }
                            }
                        }
                        ?: run {
                            move(tile, rootPosition(column, row))
                        }
                }
            }
        }

        return steps
    }

    inner class MergeableGridTile(
        value: SByte,
        pos: Position,
        var merged: Boolean = false
    ) : GridTile(context, value, pos, 1)
}