package org.oar.bytes.ui.common.components.grid.services

import android.view.View
import org.oar.bytes.features.animate.AnimationChain
import org.oar.bytes.model.Position
import org.oar.bytes.model.SByte
import org.oar.bytes.ui.animations.BumpTileAnimation
import org.oar.bytes.ui.animations.MoveTileAnimation
import org.oar.bytes.ui.common.components.grid.GridTile
import org.oar.bytes.ui.common.components.grid.model.StepAction
import org.oar.bytes.ui.common.components.grid.model.StepMerge
import org.oar.bytes.ui.common.components.grid.model.StepMove
import org.oar.bytes.utils.ListExt.findByPosition

class GridStepsGenerator(
    val parent: View
) {
    var speed: Int = 30

    fun addMoveAnimation(
        tile: GridTile,
        pos: Position,
        chain: AnimationChain = AnimationChain(tile)
    ): AnimationChain {
        return chain
            .next { MoveTileAnimation(tile, pos, speed) }
            .end { tile.pos = pos }
    }

    fun addBumpAnimation(
        tile: GridTile,
        chain: AnimationChain = AnimationChain(tile)
    ): AnimationChain {
        return chain.next { BumpTileAnimation(tile) }
    }

    private fun internalMoveTile(tiles: List<MergeableGridTile>, step: StepMove) {
        val tile = tiles.findByPosition(step.positionTile)
        tile?.pos = step.positionDest
    }

    private fun internalMergeTile(tiles: MutableList<MergeableGridTile>, step: StepMerge) {
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

    private fun createWorkableList(tiles: List<GridTile>) = tiles
        .map { MergeableGridTile(it, it.value.clone(), it.pos) }
        .toMutableList()

    fun moveLeft(
        tiles: MutableList<GridTile>,
        steps: MutableList<StepAction> = mutableListOf()
    ) = internalMove(
        tiles,
        rowsProgression = (0 until 4),
        columnsProgression = (1 until 4),
        watchPositions = { it.leftPositions },
        prevPosition = { it.right },
        rootPosition = { x, y -> Position(0, y)},
        steps = steps,
    )

    fun moveRight(
        tiles: MutableList<GridTile>,
        steps: MutableList<StepAction> = mutableListOf()
    ) = internalMove(
        tiles,
        rowsProgression = (0 until 4),
        columnsProgression = (0 until 3).reversed(),
        watchPositions = { it.rightPositions },
        prevPosition = { it.left },
        rootPosition = { x, y -> Position(3, y)},
        steps = steps,
    )

    fun moveUp(
        tiles: MutableList<GridTile>,
        steps: MutableList<StepAction> = mutableListOf()
    ) = internalMove(
        tiles,
        rowsProgression = (1 until 4),
        columnsProgression = (0 until 4),
        watchPositions = { it.topPositions },
        prevPosition = { it.bottom },
        rootPosition = { x, y -> Position(x, 0)},
        steps = steps,
    )

    fun moveDown(
        tiles: MutableList<GridTile>,
        steps: MutableList<StepAction> = mutableListOf()
    ) = internalMove(
        tiles,
        rowsProgression = (0 until 3).reversed(),
        columnsProgression = (0 until 4),
        watchPositions = { it.botPositions },
        prevPosition = { it.top },
        rootPosition = { x, y -> Position(x, 3)},
        steps = steps,
    )

    private fun internalMove(
        tiles: MutableList<GridTile>,
        rowsProgression: IntProgression,
        columnsProgression: IntProgression,
        watchPositions: (Position) -> List<Position>,
        prevPosition: (Position) -> Position?,
        rootPosition: (Int, Int) -> Position,
        steps: MutableList<StepAction>
    ): List<AnimationChain> {

        val modifyingTiles = createWorkableList(tiles)
        val animations = tiles.associateWith { AnimationChain(it) }

        fun move(tile: MergeableGridTile, pos: Position) {
            val step = StepMove(tile.pos, pos)
            internalMoveTile(modifyingTiles, step)
            steps.add(step)

            val tileOriginal = tile.originalTile

            animations[tileOriginal]
                ?.also { addMoveAnimation(tileOriginal, pos, it) }
        }

        fun merge(tileBase: MergeableGridTile, tileDest: MergeableGridTile) {
            val step = StepMerge(tileBase.pos, tileDest.pos)
            internalMergeTile(modifyingTiles, step)
            steps.add(step)

            val base = tileBase.originalTile
            val dest = tileDest.originalTile

            animations[base]
                ?.next { MoveTileAnimation(base, tileDest.pos, speed) }
                ?.next {
                    tiles.remove(dest)
                    base.pos = dest.pos
                    base.advanceTileLevel()
                    it["mergedValue"] = base.value
                    it["mergedLevel"] = base.level
                    BumpTileAnimation(base)
                }
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

        return animations.values.filter { it.hasAnimations() }
    }

    inner class MergeableGridTile(
        val originalTile: GridTile,
        value: SByte,
        pos: Position,
        var merged: Boolean = false
    ) : GridTile(parent, value, pos, 1, 0)
}