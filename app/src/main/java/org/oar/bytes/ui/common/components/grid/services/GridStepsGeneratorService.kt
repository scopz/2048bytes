package org.oar.bytes.ui.common.components.grid.services

import android.view.View
import org.oar.bytes.features.animate.AnimationChain
import org.oar.bytes.model.Position
import org.oar.bytes.model.SByte
import org.oar.bytes.ui.common.components.grid.GridTile
import org.oar.bytes.ui.common.components.grid.model.StepAction
import org.oar.bytes.ui.common.components.grid.model.StepMerge
import org.oar.bytes.ui.common.components.grid.model.StepMove
import org.oar.bytes.utils.extensions.ListExt.findActiveByPosition
import org.oar.bytes.utils.extensions.ListExt.syncAssociateWith
import org.oar.bytes.utils.extensions.ListExt.syncMap

class GridStepsGeneratorService(
    val parent: View
) {
    private val animator = GridAnimatorService(parent)
    var speed: Int = 30
        set(value) {
            field = value
            animator.speed = value
        }

    fun moveLeft(
        tiles: MutableList<GridTile>,
    ) = internalMove(
        tiles,
        rowsProgression = (0 until 4),
        columnsProgression = (1 until 4),
        watchPositions = { it.leftPositions },
        prevPosition = { it.right },
        rootPosition = { x, y -> Position(0, y)},
    )

    fun moveRight(
        tiles: MutableList<GridTile>,
    ) = internalMove(
        tiles,
        rowsProgression = (0 until 4),
        columnsProgression = (0 until 3).reversed(),
        watchPositions = { it.rightPositions },
        prevPosition = { it.left },
        rootPosition = { x, y -> Position(3, y)},
    )

    fun moveUp(
        tiles: MutableList<GridTile>,
    ) = internalMove(
        tiles,
        rowsProgression = (1 until 4),
        columnsProgression = (0 until 4),
        watchPositions = { it.topPositions },
        prevPosition = { it.bottom },
        rootPosition = { x, y -> Position(x, 0)},
    )

    fun moveDown(
        tiles: MutableList<GridTile>,
    ) = internalMove(
        tiles,
        rowsProgression = (0 until 3).reversed(),
        columnsProgression = (0 until 4),
        watchPositions = { it.botPositions },
        prevPosition = { it.top },
        rootPosition = { x, y -> Position(x, 3)},
    )

    private fun internalMove(
        tiles: MutableList<GridTile>,
        rowsProgression: IntProgression,
        columnsProgression: IntProgression,
        watchPositions: (Position) -> List<Position>,
        prevPosition: (Position) -> Position?,
        rootPosition: (Int, Int) -> Position
    ): MoveStepsWrapper {

        val stepsWrapper = MoveStepsWrapper()

        val modifyingTiles = createWorkableList(tiles)
        val animations = tiles.syncAssociateWith { AnimationChain(it) }

        fun move(tile: MergeableGridTile, pos: Position) {
            val step = StepMove(tile.pos, pos)
            internalMoveTile(modifyingTiles, step)
            stepsWrapper.addStep(step)

            val tileOriginal = tile.originalTile
            animations[tileOriginal]?.also { animator.addMoveAnimation(tileOriginal, pos, it) }
        }

        fun merge(tileBase: MergeableGridTile, tileDest: MergeableGridTile) {
            val step = StepMerge(tileBase.pos, tileDest.pos)
            internalMergeTile(modifyingTiles, step)
            stepsWrapper.addStep(step)

            val base = tileBase.originalTile
            val dest = tileDest.originalTile
            animations[base]?.also { animator.addMergeAnimation(base, dest, tileDest.pos, tiles, it) }
        }

        rowsProgression.forEach { row ->
            columnsProgression.forEach { column ->
                modifyingTiles.findActiveByPosition(Position(column, row))?.also { tile ->
                    watchPositions(tile.pos)
                        .map { modifyingTiles.findActiveByPosition(it) }
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

        return stepsWrapper.apply {
            addAnimationsChains(animations.values.filter { it.hasNext() })
        }
    }

    private fun createWorkableList(tiles: List<GridTile>) = tiles
        .syncMap { MergeableGridTile(it, it.value.clone(), it.pos) }
        .toMutableList()

    private fun internalMoveTile(tiles: List<MergeableGridTile>, step: StepMove) {
        val tile = tiles.findActiveByPosition(step.positionTile)
        tile?.pos = step.positionDest
    }

    private fun internalMergeTile(tiles: MutableList<MergeableGridTile>, step: StepMerge) {
        val tileBase = tiles.findActiveByPosition(step.positionBase)
        val tileDest = tiles.findActiveByPosition(step.positionDest)

        tiles.removeIf { it == tileBase }
        tiles.forEach {
            if (it == tileDest) {
                it.value.doubleValue()
                it.merged = true
            }
        }
    }

    inner class MergeableGridTile(
        val originalTile: GridTile,
        value: SByte,
        pos: Position,
        var merged: Boolean = false
    ) : GridTile(parent, value, pos, 1, 0)

    class MoveStepsWrapper {
        val steps = mutableListOf<StepAction>()
        val animationChain = mutableListOf<AnimationChain>()

        fun addStep(step: StepAction) = steps.add(step)
        fun addAnimationsChains(chains: List<AnimationChain>) = animationChain.addAll(chains)
    }
}